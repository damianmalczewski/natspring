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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.malczuuu.natspring.annotation.AckMode;
import io.github.malczuuu.natspring.annotation.ConsumerType;
import io.github.malczuuu.natspring.annotation.DeliverPolicyType;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JetStreamListenerEndpointRegistryTests {

  private static final Object BEAN = new Object();
  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private JetStreamListenerEndpointRegistry registry;

  @BeforeEach
  void beforeEach() {
    registry = new JetStreamListenerEndpointRegistry();
  }

  @Test
  void givenEmptyRegistry_whenGetListeners_thenReturnsEmptyList() {
    assertThat(registry.getEndpoints()).isEmpty();
  }

  @Test
  void givenListener_whenRegister_thenListenerIsReturned() {
    JetStreamListenerEndpoint endpoint =
        buildListener("orders.placed", "orders", "order-processor");

    registry.register(endpoint);

    assertThat(registry.getEndpoints()).containsExactly(endpoint);
  }

  @Test
  void givenMultipleListeners_whenRegister_thenAllListenersReturnedInOrder() {
    JetStreamListenerEndpoint first = buildListener("orders.placed", "orders", "order-processor");
    JetStreamListenerEndpoint second =
        buildListener("orders.shipped", "shipping", "ship-processor");

    registry.register(first);
    registry.register(second);

    assertThat(registry.getEndpoints()).containsExactly(first, second);
  }

  @Test
  void givenListener_whenGetListeners_thenReturnedListIsUnmodifiable() {
    registry.register(buildListener("orders.placed", "orders", "order-processor"));

    List<JetStreamListenerEndpoint> listeners = registry.getEndpoints();

    assertThatThrownBy(() -> listeners.add(buildListener("orders.shipped", "shipping", "")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenListener_whenRegister_thenMethodIsAccessible() {
    JetStreamListenerEndpoint endpoint = buildListener("orders.placed", "orders", "");

    registry.register(endpoint);

    assertThat(endpoint.getMethod().canAccess(BEAN)).isTrue();
  }

  private JetStreamListenerEndpoint buildListener(String subject, String stream, String durable) {
    return JetStreamListenerEndpoint.builder()
        .withBean(BEAN)
        .withMethod(METHOD)
        .withSubject(subject)
        .withStream(stream)
        .withDurable(durable)
        .withQueue("")
        .withConsumerType(ConsumerType.PULL)
        .withAckMode(AckMode.AUTO)
        .withDeliverPolicy(DeliverPolicyType.NEW)
        .build();
  }
}
