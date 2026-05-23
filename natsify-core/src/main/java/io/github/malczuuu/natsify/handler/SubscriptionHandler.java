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

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SubscriptionHandler implements NatsListenerHandler {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionHandler.class);

  private final Connection connection;
  private final NatsListenerHandle handle;
  private final Consumer<Message> messageConsumer;

  private boolean running = false;
  private @Nullable Dispatcher dispatcher = null;

  SubscriptionHandler(
      Connection connection, NatsListenerHandle handle, Consumer<Message> messageConsumer) {
    this.connection = connection;
    this.handle = handle;
    this.messageConsumer = messageConsumer;
  }

  @Override
  public synchronized void start() {
    if (running) {
      log.warn(
          "Attempted to call start() on already running {}",
          SubscriptionHandler.class.getSimpleName());
      return;
    }
    dispatcher = connection.createDispatcher(messageConsumer::accept);
    running = true;
    if (handle.getQueue().isEmpty()) {
      dispatcher.subscribe(handle.getSubject());
      log.info("Subscribed to NATS subject {}", handle.getSubject());
    } else {
      dispatcher.subscribe(handle.getSubject(), handle.getQueue());
      log.info("Subscribed to NATS subject {}, queue {}", handle.getSubject(), handle.getQueue());
    }
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      log.warn(
          "Attempted to call stop() on not running {}", SubscriptionHandler.class.getSimpleName());
      return;
    }
    running = false;

    Dispatcher dispatcher = this.dispatcher;
    if (dispatcher != null) {
      dispatcher.unsubscribe(handle.getSubject());
      connection.closeDispatcher(dispatcher);
      this.dispatcher = null;
    }
  }
}
