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

package io.github.malczuuu.natspring.handler;

import static io.github.malczuuu.natspring.handler.DeadLetterSupport.buildAndPublishDeadLetter;
import static io.github.malczuuu.natspring.handler.DeadLetterSupport.buildDeadLetterHeaders;

import io.github.malczuuu.natspring.annotation.AckMode;
import io.github.malczuuu.natspring.core.NatsMessageInterceptor;
import io.github.malczuuu.natspring.instrument.JetStreamListenerObserver;
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
  public void accept(Message message) {
    observer.onReceived(endpoint.getSubject(), endpoint.getStream());
    long start = System.nanoTime();
    try {
      interceptorChain.execute(message, this::doAccept);
    } catch (Exception e) {
      message.nak();
    } finally {
      observer.onProcessed(endpoint.getSubject(), endpoint.getStream(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message message) {
    Object[] args;
    try {
      args =
          messageArgumentResolver.resolveArguments(endpoint.getMethod().getParameters(), message);
    } catch (Exception e) {
      logResolutionException(message, e);
      if (!endpoint.getDeadLetterSubject().isEmpty()) {
        publishDeadLetter(message, e);
      }
      message.term();
      if (!endpoint.getDeadLetterSubject().isEmpty()) {
        observer.onDeadLettered(endpoint.getSubject(), endpoint.getStream());
      }
      observer.onTerminated(endpoint.getSubject(), endpoint.getStream(), e);
      return;
    }

    try {
      endpoint.getMethod().invoke(endpoint.getBean(), args);
      if (endpoint.getAckMode() == AckMode.AUTO) {
        message.ack();
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
        if (isLastDelivery(message)) {
          publishDeadLetter(message, cause);
          message.term();
          observer.onDeadLettered(endpoint.getSubject(), endpoint.getStream());
        } else {
          message.nak();
          observer.onNacked(endpoint.getSubject(), endpoint.getStream());
        }
      }
    }
  }

  private boolean isLastDelivery(Message message) {
    if (endpoint.getDeadLetterSubject().isEmpty()) {
      return false;
    }
    NatsJetStreamMetaData meta = message.metaData();
    return meta != null && meta.deliveredCount() >= endpoint.getMaxDeliveries();
  }

  private void publishDeadLetter(Message message, @Nullable Throwable cause) {
    Headers headers = buildDeadLetterHeaders(message, message.getSubject(), cause);
    headers.add("X-Dead-Letter-Stream", endpoint.getStream());
    headers.add("X-Dead-Letter-Durable", endpoint.getDurable());
    NatsJetStreamMetaData meta = message.metaData();
    if (meta != null) {
      headers.add("X-Dead-Letter-Delivery", String.valueOf(meta.deliveredCount()));
    }
    buildAndPublishDeadLetter(connection, endpoint.getDeadLetterSubject(), message, headers);
  }

  private void logResolutionException(Message message, Exception e) {
    String stream = null;
    Long streamSequence = null;
    Long consumerSequence = null;
    Long deliveredCount = null;
    Instant timestamp = null;

    NatsJetStreamMetaData meta = message.metaData();
    if (meta != null) {
      stream = meta.getStream();
      streamSequence = meta.streamSequence();
      consumerSequence = meta.consumerSequence();
      deliveredCount = meta.deliveredCount();
      timestamp = meta.timestamp().toInstant();
    }

    log.error(
        "Unable to resolve arguments for NATS JetStream listener={}.{}, terminating message, subject={}, stream={}, streamSequence={}, consumerSequence={}, deliveredCount={}, timestamp={}",
        AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
        endpoint.getMethod().getName(),
        message.getSubject(),
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
