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
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import java.util.ArrayList;
import java.util.List;

public class JetStreamListenerManager implements ListenerManager {

  private final JetStreamListenerRegistry registry;
  private final MessageArgumentResolver argumentResolver;
  private final JetStreamListenerObserver observer;

  private final List<JetStreamHandler> handlers = new ArrayList<>();

  public JetStreamListenerManager(
      JetStreamListenerRegistry registry,
      MessageArgumentResolver argumentResolver,
      JetStreamListenerObserver observer) {
    this.registry = registry;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
  }

  @Override
  public synchronized void initialize(Connection connection) throws Exception {
    if (registry.getListeners().isEmpty()) {
      return;
    }
    JetStream stream = connection.jetStream();
    for (JetStreamListenerDetails listener : registry.getListeners()) {
      ConsumerConfiguration configuration = buildConsumerConfiguration(listener);
      JetStreamHandler handler;
      if (listener.getConsumerType() == ConsumerType.PUSH) {
        handler = createPushHandler(connection, listener, stream, configuration);
      } else {
        handler = createPullHandler(listener, stream, configuration);
      }
      handlers.add(handler);
      handler.start();
    }
  }

  @Override
  public synchronized void stop() {
    handlers.forEach(JetStreamHandler::stop);
  }

  private JetStreamPushHandler createPushHandler(
      Connection connection,
      JetStreamListenerDetails listener,
      JetStream stream,
      ConsumerConfiguration configuration) {
    return new JetStreamPushHandler(
        connection,
        stream,
        listener,
        configuration,
        new JetStreamInvocation(listener, argumentResolver, observer));
  }

  private JetStreamPullHandler createPullHandler(
      JetStreamListenerDetails listener, JetStream stream, ConsumerConfiguration configuration) {
    return new JetStreamPullHandler(
        stream,
        listener,
        configuration,
        new JetStreamInvocation(listener, argumentResolver, observer));
  }

  private ConsumerConfiguration buildConsumerConfiguration(JetStreamListenerDetails listener) {
    ConsumerConfiguration.Builder builder =
        ConsumerConfiguration.builder()
            .deliverPolicy(toDeliverPolicy(listener.getDeliverPolicy()))
            .ackPolicy(AckPolicy.Explicit);
    if (!listener.getDurable().isEmpty()) {
      builder.durable(listener.getDurable());
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
