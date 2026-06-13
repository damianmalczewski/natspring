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

import io.github.malczuuu.natspring.core.NatsListenerMethodException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class SubscriptionHandler implements NatsListenerHandler {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionHandler.class);

  private final Connection connection;
  private final NatsListenerEndpoint endpoint;
  private final Consumer<Message> messageConsumer;

  private volatile boolean running = false;
  private volatile @Nullable Dispatcher dispatcher = null;

  SubscriptionHandler(
      Connection connection, NatsListenerEndpoint endpoint, Consumer<Message> messageConsumer) {
    this.connection = connection;
    this.endpoint = endpoint;
    this.messageConsumer = messageConsumer;
  }

  @Override
  public synchronized void start() {
    if (running) {
      throw new NatsListenerMethodException(
          "Attempted to call start() on already started "
              + SubscriptionHandler.class.getSimpleName());
    }

    Dispatcher dispatcher = connection.createDispatcher(messageConsumer::accept);
    this.dispatcher = dispatcher;
    if (endpoint.getQueue().isEmpty()) {
      dispatcher.subscribe(endpoint.getSubject());
      log.info("Subscribed to NATS subject={}", endpoint.getSubject());
    } else {
      dispatcher.subscribe(endpoint.getSubject(), endpoint.getQueue());
      log.info(
          "Subscribed to NATS subject={}, queue={}", endpoint.getSubject(), endpoint.getQueue());
    }
    running = true;
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      throw new NatsListenerMethodException(
          "Attempted to call stop() on a not-running " + SubscriptionHandler.class.getSimpleName());
    }
    running = false;

    Dispatcher dispatcher = this.dispatcher;
    if (dispatcher != null) {
      dispatcher.unsubscribe(endpoint.getSubject());
      connection.closeDispatcher(dispatcher);
      this.dispatcher = null;
    }
  }

  @Override
  public String toString() {
    return "SubscriptionHandler["
        + AopUtils.getTargetClass(endpoint.getBean()).getSimpleName()
        + "."
        + endpoint.getMethod().getName()
        + "]";
  }
}
