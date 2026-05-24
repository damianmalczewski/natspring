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

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleNatsListenerRegistryTests {

  private static final Object BEAN = new Object();
  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private SimpleNatsListenerRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new SimpleNatsListenerRegistry();
  }

  @Test
  void givenEmptyRegistry_whenGetListeners_thenReturnsEmptyList() {
    assertThat(registry.getListeners()).isEmpty();
  }

  @Test
  void givenListener_whenRegister_thenListenerIsReturned() {
    NatsListenerDetails listener = buildListener("orders.placed", "processors");

    registry.register(listener);

    assertThat(registry.getListeners()).containsExactly(listener);
  }

  @Test
  void givenMultipleListeners_whenRegister_thenAllListenersReturnedInOrder() {
    NatsListenerDetails first = buildListener("orders.placed", "processors");
    NatsListenerDetails second = buildListener("orders.shipped", "notifiers");

    registry.register(first);
    registry.register(second);

    assertThat(registry.getListeners()).containsExactly(first, second);
  }

  @Test
  void givenListener_whenGetListeners_thenReturnedListIsUnmodifiable() {
    registry.register(buildListener("orders.placed", "processors"));

    List<NatsListenerDetails> listeners = registry.getListeners();

    assertThatThrownBy(() -> listeners.add(buildListener("orders.shipped", "")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenListener_whenRegister_thenMethodIsAccessible() {
    NatsListenerDetails listener = buildListener("orders.placed", "");

    registry.register(listener);

    assertThat(listener.getMethod().canAccess(BEAN)).isTrue();
  }

  private NatsListenerDetails buildListener(String subject, String queue) {
    return NatsListenerDetails.builder()
        .withBean(BEAN)
        .withMethod(METHOD)
        .withSubject(subject)
        .withQueue(queue)
        .build();
  }
}
