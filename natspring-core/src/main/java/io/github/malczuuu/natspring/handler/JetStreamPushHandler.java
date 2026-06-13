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
  private final JetStreamListenerEndpoint endpoint;
  private final ConsumerConfiguration configuration;
  private final Consumer<Message> messageConsumer;

  private volatile boolean running = false;
  private volatile @Nullable Dispatcher dispatcher = null;
  private volatile @Nullable JetStreamSubscription subscription = null;

  JetStreamPushHandler(
      Connection connection,
      JetStream stream,
      JetStreamListenerEndpoint endpoint,
      ConsumerConfiguration configuration,
      Consumer<Message> messageConsumer) {
    this.connection = connection;
    this.stream = stream;
    this.endpoint = endpoint;
    this.configuration = configuration;
    this.messageConsumer = messageConsumer;
  }

  @Override
  public synchronized void start() throws IOException, JetStreamApiException {
    if (running) {
      throw new NatsListenerMethodException(
          "Attempted to call start() on already started "
              + JetStreamPushHandler.class.getSimpleName());
    }

    PushSubscribeOptions.Builder builder =
        PushSubscribeOptions.builder().configuration(configuration);
    if (!endpoint.getStream().isEmpty()) {
      builder.stream(endpoint.getStream());
    }
    PushSubscribeOptions options = builder.build();
    Dispatcher dispatcher = connection.createDispatcher();
    this.dispatcher = dispatcher;
    try {
      if (endpoint.getQueue().isEmpty()) {
        subscription =
            stream.subscribe(
                endpoint.getSubject(), dispatcher, messageConsumer::accept, false, options);
        log.info(
            "Subscribed push JetStream listener to stream={}, subject={}",
            endpoint.getStream(),
            endpoint.getSubject());
      } else {
        subscription =
            stream.subscribe(
                endpoint.getSubject(),
                endpoint.getQueue(),
                dispatcher,
                messageConsumer::accept,
                false,
                options);
        log.info(
            "Subscribed push JetStream listener to stream={}, subject={}, queue={}",
            endpoint.getStream(),
            endpoint.getSubject(),
            endpoint.getQueue());
      }
    } catch (Exception e) {
      connection.closeDispatcher(dispatcher);
      this.dispatcher = null;
      throw e;
    }
    running = true;
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      throw new NatsListenerMethodException(
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
        + AopUtils.getTargetClass(endpoint.getBean()).getSimpleName()
        + "."
        + endpoint.getMethod().getName()
        + "]";
  }
}
