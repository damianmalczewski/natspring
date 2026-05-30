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

import io.github.malczuuu.natspring.core.NatsMessageInterceptor;
import io.github.malczuuu.natspring.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class NatsListenerInvocation implements Consumer<Message> {

  private static final Logger log = LoggerFactory.getLogger(NatsListenerInvocation.class);

  private final Connection connection;
  private final MessageArgumentResolver argumentResolver;
  private final NatsListenerObserver observer;
  private final NatsListenerEndpoint endpoint;
  private final NatsMessageInterceptorChainExecution interceptorChain;

  NatsListenerInvocation(
      Connection connection,
      MessageArgumentResolver argumentResolver,
      NatsListenerObserver observer,
      NatsListenerEndpoint endpoint,
      List<NatsMessageInterceptor> interceptors) {
    this.connection = connection;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
    this.endpoint = endpoint;
    this.interceptorChain = new NatsMessageInterceptorChainExecution(interceptors);
  }

  @Override
  public void accept(Message message) {
    observer.onReceived(endpoint.getSubject(), endpoint.getQueue());
    long start = System.nanoTime();
    try {
      interceptorChain.execute(message, this::doAccept);
    } finally {
      observer.onProcessed(endpoint.getSubject(), endpoint.getQueue(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message message) {
    Object[] args;
    try {
      args = argumentResolver.resolveArguments(endpoint.getMethod().getParameters(), message);
    } catch (Exception e) {
      log.error(
          "Unable to resolve arguments for NATS listener={}.{}, dropping message, subject={}",
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName(),
          message.getSubject(),
          e);
      observer.onFailed(endpoint.getSubject(), endpoint.getQueue());
      publishDeadLetter(message, e);
      return;
    }

    try {
      Object result = endpoint.getMethod().invoke(endpoint.getBean(), args);
      observer.onSucceeded(endpoint.getSubject(), endpoint.getQueue());
      publishReply(message, result);
    } catch (InvocationTargetException | IllegalAccessException e) {
      Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
      log.error(
          "Failed to invoke handler for NATS listener={}.{}",
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName(),
          cause);
      observer.onFailed(endpoint.getSubject(), endpoint.getQueue());
      publishDeadLetter(message, cause);
    }
  }

  private void publishReply(Message message, @Nullable Object result) {
    if (result == null) {
      return;
    }
    String replyTo = message.getReplyTo();
    if (replyTo == null || replyTo.isEmpty()) {
      log.warn(
          "NATS listener={}.{} returned a value but the message has no reply-to address, discarding reply",
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName());
      observer.onReplyDiscarded(endpoint.getSubject(), endpoint.getQueue());
      return;
    }
    try {
      connection.publish(argumentResolver.buildReplyMessage(result, replyTo));
    } catch (Exception e) {
      log.error(
          "Failed to publish reply to subject={} for NATS listener={}.{}",
          replyTo,
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName(),
          e);
      observer.onReplyFailed(endpoint.getSubject(), endpoint.getQueue());
    }
  }

  private void publishDeadLetter(Message message, @Nullable Throwable cause) {
    if (endpoint.getDeadLetterSubject().isEmpty()) {
      return;
    }
    try {
      Headers headers = buildDeadLetterHeaders(message, message.getSubject(), cause);
      buildAndPublishDeadLetter(connection, endpoint.getDeadLetterSubject(), message, headers);
      observer.onDeadLettered(endpoint.getSubject(), endpoint.getQueue());
    } catch (Exception e) {
      log.error(
          "Failed to publish dead-letter message to subject={}, listener={}.{}",
          endpoint.getDeadLetterSubject(),
          AopUtils.getTargetClass(endpoint.getBean()).getSimpleName(),
          endpoint.getMethod().getName(),
          e);
    }
  }

  @Override
  public String toString() {
    return "NatsListenerInvocation["
        + AopUtils.getTargetClass(endpoint.getBean()).getSimpleName()
        + "."
        + endpoint.getMethod().getName()
        + "]";
  }
}
