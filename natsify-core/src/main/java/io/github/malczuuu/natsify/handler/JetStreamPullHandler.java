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

import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JetStreamPullHandler implements JetStreamHandler {

  private static final Logger log = LoggerFactory.getLogger(JetStreamPullHandler.class);

  private final JetStream stream;
  private final JetStreamListenerDetails listener;
  private final ConsumerConfiguration configuration;
  private final Consumer<Message> messageConsumer;

  private boolean running = false;
  private @Nullable JetStreamSubscription subscription = null;
  private @Nullable Thread listenerThread = null;

  JetStreamPullHandler(
      JetStream stream,
      JetStreamListenerDetails listener,
      ConsumerConfiguration configuration,
      Consumer<Message> messageConsumer) {
    this.stream = stream;
    this.listener = listener;
    this.configuration = configuration;
    this.messageConsumer = messageConsumer;
  }

  @Override
  public synchronized void start() throws IOException, JetStreamApiException {
    if (running) {
      log.warn(
          "Attempted to call start() on already running {}",
          JetStreamPullHandler.class.getSimpleName());
      return;
    }

    PullSubscribeOptions.Builder builder =
        PullSubscribeOptions.builder().configuration(configuration);
    if (!listener.getStream().isEmpty()) {
      builder.stream(listener.getStream());
    }
    subscription = stream.subscribe(listener.getSubject(), builder.build());
    running = true;
    listenerThread = new Thread(this::runPollPool, "nats-pull-" + listener.getSubject());
    listenerThread.setDaemon(true);
    listenerThread.start();
    log.info("Subscribed pull JetStream listener to subject {}", listener.getSubject());
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      log.warn(
          "Attempted to call stop() on not running {}", JetStreamPullHandler.class.getSimpleName());
      return;
    }
    running = false;

    Thread listenerThread = this.listenerThread;
    if (listenerThread != null) {
      listenerThread.interrupt();
    }

    JetStreamSubscription subscription = this.subscription;
    if (subscription != null) {
      subscription.unsubscribe();
      this.subscription = null;
    }
  }

  private void runPollPool() {
    JetStreamSubscription sub = subscription;
    if (sub == null) {
      return;
    }
    while (running && !Thread.currentThread().isInterrupted()) {
      try {
        List<Message> messages = sub.fetch(10, Duration.ofSeconds(1));
        for (Message msg : messages) {
          messageConsumer.accept(msg);
        }
      } catch (Exception e) {
        if (!running || Thread.currentThread().isInterrupted()) {
          return;
        }
        log.error("Error polling JetStream messages for subject {}", listener.getSubject(), e);
      }
    }
  }
}
