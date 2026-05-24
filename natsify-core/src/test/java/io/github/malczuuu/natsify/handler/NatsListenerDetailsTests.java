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
import org.junit.jupiter.api.Test;

class NatsListenerDetailsTests {

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
    NatsListenerDetails listener =
        NatsListenerDetails.builder()
            .withBean(BEAN)
            .withMethod(METHOD)
            .withSubject("orders.placed")
            .withQueue("processors")
            .build();

    assertThat(listener.getBean()).isSameAs(BEAN);
    assertThat(listener.getMethod()).isSameAs(METHOD);
    assertThat(listener.getSubject()).isEqualTo("orders.placed");
    assertThat(listener.getQueue()).isEqualTo("processors");
  }

  @Test
  void givenMissingBean_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(
            () ->
                NatsListenerDetails.builder()
                    .withMethod(METHOD)
                    .withSubject("orders.placed")
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("bean is required");
  }

  @Test
  void givenMissingMethod_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(
            () ->
                NatsListenerDetails.builder()
                    .withBean(BEAN)
                    .withSubject("orders.placed")
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("method is required");
  }

  @Test
  void givenMissingSubject_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(
            () ->
                NatsListenerDetails.builder()
                    .withBean(BEAN)
                    .withMethod(METHOD)
                    .withQueue("processors")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("subject is required");
  }

  @Test
  void givenMissingQueue_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(
            () ->
                NatsListenerDetails.builder()
                    .withBean(BEAN)
                    .withMethod(METHOD)
                    .withSubject("orders.placed")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("queue is required");
  }
}
