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

import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class NatsListenerInvocation implements Consumer<Message> {

  private static final Logger log = LoggerFactory.getLogger(NatsListenerInvocation.class);

  private final Connection connection;
  private final MessageArgumentResolver argumentResolver;
  private final NatsListenerObserver observer;
  private final NatsListenerDetails listener;

  NatsListenerInvocation(
      Connection connection,
      MessageArgumentResolver argumentResolver,
      NatsListenerObserver observer,
      NatsListenerDetails listener) {
    this.connection = connection;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
    this.listener = listener;
  }

  @Override
  public void accept(Message msg) {
    observer.onReceived(listener.getSubject(), listener.getQueue());
    long start = System.nanoTime();
    try {
      doAccept(msg);
    } finally {
      observer.onProcessed(listener.getSubject(), listener.getQueue(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message msg) {
    Object[] args;
    try {
      args = argumentResolver.resolveArguments(listener.getMethod().getParameters(), msg);
    } catch (Exception e) {
      log.error(
          "Unable to resolve arguments for NATS listener {}.{}, dropping message, subject={}",
          AopUtils.getTargetClass(listener.getBean()).getSimpleName(),
          listener.getMethod().getName(),
          msg.getSubject(),
          e);
      observer.onFailed(listener.getSubject(), listener.getQueue());
      publishDeadLetter(msg, e);
      return;
    }

    try {
      listener.getMethod().invoke(listener.getBean(), args);
      observer.onSucceeded(listener.getSubject(), listener.getQueue());
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.error("Failed to invoke handler for NATS listener {}", listener.getMethod(), e);
      observer.onFailed(listener.getSubject(), listener.getQueue());
      publishDeadLetter(msg, e);
    }
  }

  private void publishDeadLetter(Message msg, Exception cause) {
    if (listener.getDeadLetterSubject().isEmpty()) {
      return;
    }
    try {
      Headers headers = buildDeadLetterHeaders(msg, msg.getSubject(), cause);
      buildAndPublishDeadLetter(connection, listener.getDeadLetterSubject(), msg, headers);
      observer.onDeadLettered(listener.getSubject(), listener.getQueue());
    } catch (Exception e) {
      log.error(
          "Failed to publish dead-letter message to subject {}",
          listener.getDeadLetterSubject(),
          e);
    }
  }

  @Override
  public String toString() {
    return "NatsListenerInvocation["
        + AopUtils.getTargetClass(listener.getBean()).getSimpleName()
        + "."
        + listener.getMethod().getName()
        + "]";
  }
}
