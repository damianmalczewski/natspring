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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.malczuuu.natsify.annotation.AckMode;
import io.github.malczuuu.natsify.annotation.ConsumerType;
import io.github.malczuuu.natsify.annotation.DeliverPolicyType;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleJetStreamListenerRegistryTests {

  private static final Object BEAN = new Object();
  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private SimpleJetStreamListenerRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleJetStreamListenerRegistry();
  }

  @Test
  void givenEmptyRegistry_whenGetListeners_thenReturnsEmptyList() {
    assertThat(registry.getListeners()).isEmpty();
  }

  @Test
  void givenListener_whenRegister_thenListenerIsReturned() {
    JetStreamListenerDetails listener = buildListener("orders.placed", "orders", "order-processor");

    registry.register(listener);

    assertThat(registry.getListeners()).containsExactly(listener);
  }

  @Test
  void givenMultipleListeners_whenRegister_thenAllListenersReturnedInOrder() {
    JetStreamListenerDetails first = buildListener("orders.placed", "orders", "order-processor");
    JetStreamListenerDetails second = buildListener("orders.shipped", "shipping", "ship-processor");

    registry.register(first);
    registry.register(second);

    assertThat(registry.getListeners()).containsExactly(first, second);
  }

  @Test
  void givenListener_whenGetListeners_thenReturnedListIsUnmodifiable() {
    registry.register(buildListener("orders.placed", "orders", "order-processor"));

    List<JetStreamListenerDetails> listeners = registry.getListeners();

    assertThatThrownBy(() -> listeners.add(buildListener("orders.shipped", "shipping", "")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenListener_whenRegister_thenMethodIsAccessible() {
    JetStreamListenerDetails listener = buildListener("orders.placed", "orders", "");

    registry.register(listener);

    assertThat(listener.getMethod().canAccess(BEAN)).isTrue();
  }

  private JetStreamListenerDetails buildListener(String subject, String stream, String durable) {
    return JetStreamListenerDetails.builder()
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
