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

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class NatsListenerEndpointTests {

  private static final Object BEAN = new Object();
  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void givenAllFields_whenBuild_thenHandleCreatedWithCorrectValues() {
    NatsListenerEndpoint endpoint =
        NatsListenerEndpoint.builder()
            .withBean(BEAN)
            .withMethod(METHOD)
            .withSubject("orders.placed")
            .withQueue("processors")
            .withDeadLetterSubject("orders.placed.dlq")
            .build();

    assertThat(endpoint.getBean()).isSameAs(BEAN);
    assertThat(endpoint.getMethod()).isSameAs(METHOD);
    assertThat(endpoint.getSubject()).isEqualTo("orders.placed");
    assertThat(endpoint.getQueue()).isEqualTo("processors");
    assertThat(endpoint.getDeadLetterSubject()).isEqualTo("orders.placed.dlq");
  }

  @Test
  void givenNoDeadLetterSubject_whenBuild_thenDeadLetterSubjectIsEmpty() {
    NatsListenerEndpoint endpoint =
        NatsListenerEndpoint.builder()
            .withBean(BEAN)
            .withMethod(METHOD)
            .withSubject("orders.placed")
            .withQueue("processors")
            .build();

    assertThat(endpoint.getDeadLetterSubject()).isEmpty();
  }

  @Test
  void givenMissingBean_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                NatsListenerEndpoint.builder()
                    .withMethod(METHOD)
                    .withSubject("orders.placed")
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("bean is required");
  }

  @Test
  void givenMissingMethod_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                NatsListenerEndpoint.builder()
                    .withBean(BEAN)
                    .withSubject("orders.placed")
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("method is required");
  }

  @Test
  void givenMissingSubject_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                NatsListenerEndpoint.builder()
                    .withBean(BEAN)
                    .withMethod(METHOD)
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("subject is required");
  }

  @Test
  void givenMissingQueue_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                NatsListenerEndpoint.builder()
                    .withBean(BEAN)
                    .withMethod(METHOD)
                    .withSubject("orders.placed")
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("queue is required");
  }
}
