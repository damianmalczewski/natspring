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
import org.springframework.aop.support.AopUtils;

final class JetStreamPullHandler implements JetStreamHandler {

  private static final Logger log = LoggerFactory.getLogger(JetStreamPullHandler.class);

  private final JetStream stream;
  private final JetStreamListenerDetails listener;
  private final ConsumerConfiguration configuration;
  private final Consumer<Message> messageConsumer;

  private final int fetchBatchSize;
  private final Duration fetchTimeout;

  private volatile boolean running = false;
  private volatile @Nullable JetStreamSubscription subscription = null;
  private volatile @Nullable Thread listenerThread = null;

  JetStreamPullHandler(
      JetStream stream,
      JetStreamListenerDetails listener,
      ConsumerConfiguration configuration,
      Consumer<Message> messageConsumer,
      int fetchBatchSize,
      Duration fetchTimeout) {
    this.stream = stream;
    this.listener = listener;
    this.configuration = configuration;
    this.messageConsumer = messageConsumer;
    this.fetchBatchSize = fetchBatchSize;
    this.fetchTimeout = fetchTimeout;
  }

  @Override
  public synchronized void start() throws IOException, JetStreamApiException {
    if (running) {
      throw new ListenerConfigureException(
          "Attempted to call start() on already started "
              + JetStreamPullHandler.class.getSimpleName());
    }

    PullSubscribeOptions.Builder builder =
        PullSubscribeOptions.builder().configuration(configuration);
    if (!listener.getStream().isEmpty()) {
      builder.stream(listener.getStream());
    }
    subscription = stream.subscribe(listener.getSubject(), builder.build());

    Thread listenerThread = new Thread(this::runPollPool, "nats-pull-" + listener.getSubject());
    this.listenerThread = listenerThread;
    listenerThread.setDaemon(true);
    running = true;
    listenerThread.start();

    log.info("Subscribed pull JetStream listener to subject {}", listener.getSubject());
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      throw new ListenerConfigureException(
          "Attempted to call stop() on a not-running "
              + JetStreamPullHandler.class.getSimpleName());
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
        List<Message> messages = sub.fetch(fetchBatchSize, fetchTimeout);
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

  @Override
  public String toString() {
    return "JetStreamPullHandler["
        + AopUtils.getTargetClass(listener.getBean()).getSimpleName()
        + "."
        + listener.getMethod().getName()
        + "]";
  }
}
