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

import io.github.malczuuu.natsify.core.NatsMessageInterceptor;
import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages NATS Core subscription handlers for registered {@link NatsListenerEndpoint endpoints}.
 *
 * @since 0.1.0
 */
public class NatsMessageListenerContainer implements MessageListenerContainer {

  private final NatsListenerEndpointRegistry registry;
  private final MessageArgumentResolver argumentResolver;
  private final NatsListenerObserver observer;
  private final List<NatsMessageInterceptor> interceptors;

  private final List<NatsListenerHandler> handlers = new CopyOnWriteArrayList<>();

  /**
   * Creates a new {@code NatsMessageListenerContainer} with interceptors.
   *
   * @param registry registry of listener endpoints to initialize
   * @param argumentResolver resolver used to map message data to handler method arguments
   * @param observer observer notified on listener invocations
   * @param interceptors interceptors applied before each listener method invocation
   * @since 0.1.0
   */
  public NatsMessageListenerContainer(
      NatsListenerEndpointRegistry registry,
      MessageArgumentResolver argumentResolver,
      NatsListenerObserver observer,
      List<NatsMessageInterceptor> interceptors) {
    this.registry = registry;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
    this.interceptors = interceptors;
  }

  /**
   * Initializes and starts all handlers using the given NATS connection. Creates a subscription
   * handler for each registered {@link NatsListenerEndpoint}.
   *
   * @param connection the active NATS connection
   * @throws Exception if any handler fails to start
   * @since 0.1.0
   */
  @Override
  public synchronized void start(Connection connection) throws Exception {
    for (NatsListenerEndpoint endpoint : registry.getEndpoints()) {
      NatsListenerHandler handler =
          new SubscriptionHandler(
              connection,
              endpoint,
              new NatsListenerInvocation(
                  connection, argumentResolver, observer, endpoint, interceptors));
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
    for (NatsListenerHandler handler : handlers) {
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
}
