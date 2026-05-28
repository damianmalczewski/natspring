/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.malczuuu.natsify.handler;

import static io.github.malczuuu.natsify.handler.DeadLetterSupport.buildAndPublishDeadLetter;
import static io.github.malczuuu.natsify.handler.DeadLetterSupport.buildDeadLetterHeaders;

import io.github.malczuuu.natsify.annotation.AckMode;
import io.github.malczuuu.natsify.core.NatsMessageInterceptor;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class JetStreamInvocation implements Consumer<Message> {

  private static final Logger log = LoggerFactory.getLogger(JetStreamInvocation.class);

  private final Connection connection;
  private final JetStreamListenerEndpoint endpoint;
  private final MessageArgumentResolver messageArgumentResolver;
  private final JetStreamListenerObserver observer;
  private final NatsMessageInterceptorChainExecution interceptorChain;

  JetStreamInvocation(
      Connection connection,
      MessageArgumentResolver argumentResolver,
      JetStreamListenerObserver observer,
      JetStreamListenerEndpoint endpoint,
      List<NatsMessageInterceptor> interceptors) {
    this.connection = connection;
    this.messageArgumentResolver = argumentResolver;
    this.observer = observer;
    this.endpoint = endpoint;
    this.interceptorChain = new NatsMessageInterceptorChainExecution(interceptors);
  }

  @Override
  public void accept(Message msg) {
    observer.onReceived(endpoint.getSubject(), endpoint.getStream());
    long start = System.nanoTime();
    try {
      interceptorChain.execute(msg, this::doAccept);
    } catch (Exception e) {
      msg.nak();
    } finally {
      observer.onProcessed(endpoint.getSubject(), endpoint.getStream(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message msg) {
    Object[] args;
    try {
      args = messageArgumentResolver.resolveArguments(endpoint.getMethod().getParameters(), msg);
    } catch (Exception e) {
      logResolutionException(msg, e);
      if (!endpoint.getDeadLetterSubject().isEmpty()) {
        publishDeadLetter(msg, e);
        observer.onDeadLettered(endpoint.getSubject(), endpoint.getStream());
      }
      msg.term();
      observer.onTerminated(endpoint.getSubject(), endpoint.getStream(), e);
      return;
    }

    try {
      endpoint.getMethod().invoke(endpoint.getBean(), args);
      if (endpoint.getAckMode() == AckMode.AUTO) {
        msg.ack();
        observer.onAcked(endpoint.getSubject(), endpoint.getStream());
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
      log.error(
          "Failed to invoke handler for NATS JetStream listener={}.{}",
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName(),
          cause);
      if (endpoint.getAckMode() == AckMode.AUTO) {
        if (isLastDelivery(msg)) {
          publishDeadLetter(msg, cause instanceof Exception ex ? ex : e);
          observer.onDeadLettered(endpoint.getSubject(), endpoint.getStream());
          msg.term();
        } else {
          msg.nak();
          observer.onNacked(endpoint.getSubject(), endpoint.getStream());
        }
      }
    }
  }

  private boolean isLastDelivery(Message msg) {
    if (endpoint.getDeadLetterSubject().isEmpty()) {
      return false;
    }
    NatsJetStreamMetaData meta = msg.metaData();
    return meta != null && meta.deliveredCount() >= endpoint.getMaxDeliveries();
  }

  private void publishDeadLetter(Message msg, @Nullable Exception cause) {
    Headers headers = buildDeadLetterHeaders(msg, msg.getSubject(), cause);
    headers.add("X-Dead-Letter-Stream", endpoint.getStream());
    headers.add("X-Dead-Letter-Durable", endpoint.getDurable());
    NatsJetStreamMetaData meta = msg.metaData();
    if (meta != null) {
      headers.add("X-Dead-Letter-Delivery", String.valueOf(meta.deliveredCount()));
    }
    buildAndPublishDeadLetter(connection, endpoint.getDeadLetterSubject(), msg, headers);
  }

  private void logResolutionException(Message msg, Exception e) {
    String stream = null;
    Long streamSequence = null;
    Long consumerSequence = null;
    Long deliveredCount = null;
    Instant timestamp = null;

    if (msg.metaData() != null) {
      stream = msg.metaData().getStream();
      streamSequence = msg.metaData().streamSequence();
      consumerSequence = msg.metaData().consumerSequence();
      deliveredCount = msg.metaData().deliveredCount();
      timestamp = msg.metaData().timestamp().toInstant();
    }

    log.error(
        "Unable to resolve arguments for NATS JetStream listener={}.{}, terminating message, subject={}, stream={}, streamSequence={}, consumerSequence={}, deliveredCount={}, timestamp={}",
        AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
        endpoint.getMethod().getName(),
        msg.getSubject(),
        stream,
        streamSequence,
        consumerSequence,
        deliveredCount,
        timestamp,
        e);
  }

  @Override
  public String toString() {
    return "JetStreamInvocation["
        + AopUtils.getTargetClass(endpoint.getBean()).getSimpleName()
        + "."
        + endpoint.getMethod().getName()
        + "]";
  }
}
