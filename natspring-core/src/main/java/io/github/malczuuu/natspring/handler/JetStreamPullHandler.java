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

import io.github.malczuuu.natspring.core.ListenerConfigureException;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class JetStreamPullHandler implements JetStreamHandler {

  private static final Logger log = LoggerFactory.getLogger(JetStreamPullHandler.class);

  private final JetStream stream;
  private final JetStreamListenerEndpoint endpoint;
  private final ConsumerConfiguration configuration;
  private final Consumer<Message> messageConsumer;

  private final int fetchBatchSize;
  private final Duration fetchTimeout;

  private volatile boolean running = false;
  private volatile @Nullable JetStreamSubscription subscription = null;
  private volatile @Nullable ExecutorService executor = null;

  JetStreamPullHandler(
      JetStream stream,
      JetStreamListenerEndpoint endpoint,
      ConsumerConfiguration configuration,
      Consumer<Message> messageConsumer,
      int fetchBatchSize,
      Duration fetchTimeout) {
    this.stream = stream;
    this.endpoint = endpoint;
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
    if (!endpoint.getStream().isEmpty()) {
      builder.stream(endpoint.getStream());
    }
    subscription = stream.subscribe(endpoint.getSubject(), builder.build());

    String threadName = "nats-pull-" + endpoint.getSubject();
    ExecutorService executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, threadName);
              t.setDaemon(true);
              return t;
            });
    this.executor = executor;
    running = true;
    executor.execute(this::runPollPool);

    log.info(
        "Subscribed pull JetStream listener to stream={}, subject={}",
        endpoint.getStream(),
        endpoint.getSubject());
  }

  @Override
  public synchronized void stop() {
    if (!running) {
      throw new ListenerConfigureException(
          "Attempted to call stop() on a not-running "
              + JetStreamPullHandler.class.getSimpleName());
    }
    running = false;

    ExecutorService executor = this.executor;
    if (executor != null) {
      executor.shutdownNow();
      try {
        int i = 0;
        while (!executor.awaitTermination(5, TimeUnit.SECONDS) && i++ < 3) {
          log.warn(
              "Pull JetStream listener thread did not terminate after 5 seconds, waiting again (count={} attempts so far)",
              i);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
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
        for (Message message : messages) {
          messageConsumer.accept(message);
        }
      } catch (Exception e) {
        if (!running || Thread.currentThread().isInterrupted()) {
          return;
        }
        log.error("Error polling JetStream messages for subject={}", endpoint.getSubject(), e);
      }
    }
  }

  @Override
  public String toString() {
    return "JetStreamPullHandler["
        + AopUtils.getTargetClass(endpoint.getBean()).getSimpleName()
        + "."
        + endpoint.getMethod().getName()
        + "]";
  }
}
