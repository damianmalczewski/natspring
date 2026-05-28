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

import io.github.malczuuu.natsify.annotation.ConsumerType;
import io.github.malczuuu.natsify.annotation.DeliverPolicyType;
import io.github.malczuuu.natsify.core.NatsMessageInterceptor;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages JetStream consumer handlers for registered {@link JetStreamListenerEndpoint endpoints}.
 *
 * @since 0.1.0
 */
public class JetStreamMessageListenerContainer implements MessageListenerContainer {

  private final JetStreamListenerEndpointRegistry registry;
  private final MessageArgumentResolver argumentResolver;
  private final JetStreamListenerObserver observer;
  private final List<NatsMessageInterceptor> interceptors;

  private final int pullFetchBatchSize;
  private final Duration pullFetchTimeout;

  private final List<JetStreamHandler> handlers = new CopyOnWriteArrayList<>();

  /**
   * Creates a new {@code JetStreamMessageListenerContainer} with interceptors.
   *
   * @param registry registry of listener endpoints to initialize
   * @param argumentResolver resolver used to map message data to handler method arguments
   * @param observer observer notified on listener invocations
   * @param pullFetchBatchSize number of messages to fetch per poll cycle for pull consumers
   * @param pullFetchTimeout maximum time to wait for messages in each fetch call for pull consumers
   * @param interceptors interceptors applied before each listener method invocation
   * @since 0.1.0
   */
  public JetStreamMessageListenerContainer(
      JetStreamListenerEndpointRegistry registry,
      MessageArgumentResolver argumentResolver,
      JetStreamListenerObserver observer,
      int pullFetchBatchSize,
      Duration pullFetchTimeout,
      List<NatsMessageInterceptor> interceptors) {
    this.registry = registry;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
    this.pullFetchBatchSize = pullFetchBatchSize;
    this.pullFetchTimeout = pullFetchTimeout;
    this.interceptors = interceptors;
  }

  /**
   * Initializes and starts all handlers using the given NATS connection. Creates a push or pull
   * consumer handler for each registered {@link JetStreamListenerEndpoint}. Does nothing if no
   * listeners are registered.
   *
   * @param connection the active NATS connection
   * @throws Exception if any handler fails to start
   * @since 0.1.0
   */
  @Override
  public synchronized void start(Connection connection) throws Exception {
    if (registry.getEndpoints().isEmpty()) {
      return;
    }
    JetStream stream = connection.jetStream();
    for (JetStreamListenerEndpoint endpoint : registry.getEndpoints()) {
      ConsumerConfiguration configuration = buildConsumerConfiguration(endpoint);
      JetStreamHandler handler;
      if (endpoint.getConsumerType() == ConsumerType.PUSH) {
        handler = createPushHandler(connection, endpoint, stream, configuration);
      } else {
        handler = createPullHandler(connection, endpoint, stream, configuration);
      }
      handlers.add(handler);
      handler.start();
    }
  }

  /**
   * Stops all active handlers. Attempts to stop every handler before propagating failures.
   *
   * @since 0.1.0
   */
  @Override
  public synchronized void stop() {
    List<RuntimeException> failures = new ArrayList<>();
    for (JetStreamHandler handler : handlers) {
      try {
        handler.stop();
      } catch (RuntimeException e) {
        failures.add(e);
      }
    }
    handlers.clear();
    if (!failures.isEmpty()) {
      RuntimeException first = failures.get(0);
      if (failures.size() == 1) {
        throw first;
      }
      IllegalStateException composite =
          new IllegalStateException(
              failures.size() + " handler(s) failed to stop: " + summarize(failures), first);
      failures.stream().skip(1).forEach(composite::addSuppressed);
      throw composite;
    }
  }

  private static String summarize(List<RuntimeException> failures) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < failures.size(); i++) {
      if (i > 0) {
        sb.append("; ");
      }
      sb.append('[').append(i).append("] ").append(failures.get(i).getMessage());
    }
    return sb.toString();
  }

  private JetStreamPushHandler createPushHandler(
      Connection connection,
      JetStreamListenerEndpoint endpoint,
      JetStream stream,
      ConsumerConfiguration configuration) {
    return new JetStreamPushHandler(
        connection,
        stream,
        endpoint,
        configuration,
        new JetStreamInvocation(connection, argumentResolver, observer, endpoint, interceptors));
  }

  private JetStreamPullHandler createPullHandler(
      Connection connection,
      JetStreamListenerEndpoint endpoint,
      JetStream stream,
      ConsumerConfiguration configuration) {
    return new JetStreamPullHandler(
        stream,
        endpoint,
        configuration,
        new JetStreamInvocation(connection, argumentResolver, observer, endpoint, interceptors),
        pullFetchBatchSize,
        pullFetchTimeout);
  }

  private ConsumerConfiguration buildConsumerConfiguration(JetStreamListenerEndpoint endpoint) {
    ConsumerConfiguration.Builder builder =
        ConsumerConfiguration.builder()
            .deliverPolicy(toDeliverPolicy(endpoint.getDeliverPolicy()))
            .ackPolicy(AckPolicy.Explicit);
    if (!endpoint.getDurable().isEmpty()) {
      builder.durable(endpoint.getDurable());
    }
    if (endpoint.getMaxDeliveries() > 0) {
      builder.maxDeliver(endpoint.getMaxDeliveries());
    }
    return builder.build();
  }

  private DeliverPolicy toDeliverPolicy(DeliverPolicyType deliverPolicyType) {
    return switch (deliverPolicyType) {
      case ALL -> DeliverPolicy.All;
      case NEW -> DeliverPolicy.New;
      case LAST -> DeliverPolicy.Last;
    };
  }
}
