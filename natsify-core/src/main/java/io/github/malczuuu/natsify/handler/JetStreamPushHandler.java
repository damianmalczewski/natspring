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

import io.github.malczuuu.natsify.core.ListenerConfigureException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import java.io.IOException;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class JetStreamPushHandler implements JetStreamHandler {

  private static final Logger log = LoggerFactory.getLogger(JetStreamPushHandler.class);

  private final Connection connection;
  private final JetStream stream;
  private final JetStreamListenerDetails listener;
  private final ConsumerConfiguration configuration;
  private final Consumer<Message> messageConsumer;

  private volatile boolean running = false;
  private volatile @Nullable Dispatcher dispatcher = null;
  private volatile @Nullable JetStreamSubscription subscription = null;

  JetStreamPushHandler(
      Connection connection,
      JetStream stream,
      JetStreamListenerDetails listener,
      ConsumerConfiguration configuration,
      Consumer<Message> messageConsumer) {
    this.connection = connection;
    this.stream = stream;
    this.listener = listener;
    this.configuration = configuration;
    this.messageConsumer = messageConsumer;
  }

  @Override
  public synchronized void start() throws IOException, JetStreamApiException {
    if (running) {
      throw new ListenerConfigureException(
          "Attempted to call start() on already started "
              + JetStreamPushHandler.class.getSimpleName());
    }

    PushSubscribeOptions.Builder builder =
        PushSubscribeOptions.builder().configuration(configuration);
    if (!listener.getStream().isEmpty()) {
      builder.stream(listener.getStream());
    }
    PushSubscribeOptions options = builder.build();
    dispatcher = connection.createDispatcher();
    if (listener.getQueue().isEmpty()) {
      subscription =
          stream.subscribe(
              listener.getSubject(), dispatcher, messageConsumer::accept, false, options);
      log.info("Subscribed push JetStream listener to subject {}", listener.getSubject());
    } else {
      subscription =
          stream.subscribe(
              listener.getSubject(),
              listener.getQueue(),
              dispatcher,
              messageConsumer::accept,
              false,
              options);
      log.info(
          "Subscribed push JetStream listener to subject {}, queue {}",
          listener.getSubject(),
          listener.getQueue());
    }
    running = true;
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      throw new ListenerConfigureException(
          "Attempted to call stop() on a not-running "
              + JetStreamPushHandler.class.getSimpleName());
    }
    running = false;

    Dispatcher dispatcher = this.dispatcher;
    JetStreamSubscription subscription = this.subscription;

    if (dispatcher != null) {
      if (subscription != null) {
        dispatcher.unsubscribe(subscription);
      }
      connection.closeDispatcher(dispatcher);
      this.dispatcher = null;
    }
  }

  @Override
  public String toString() {
    return "JetStreamPushHandler["
        + AopUtils.getTargetClass(listener.getBean()).getSimpleName()
        + "."
        + listener.getMethod().getName()
        + "]";
  }
}
