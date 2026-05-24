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
import org.junit.jupiter.api.Test;

class JetStreamListenerHandleBuilderTests {

  private static final Object BEAN = new Object();
  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private JetStreamListenerDetails.Builder fullBuilder() {
    return JetStreamListenerDetails.builder()
        .withBean(BEAN)
        .withMethod(METHOD)
        .withSubject("orders.>")
        .withStream("ORDERS")
        .withDurable("order-processor")
        .withQueue("")
        .withConsumerType(ConsumerType.PUSH)
        .withAckMode(AckMode.AUTO)
        .withDeliverPolicy(DeliverPolicyType.ALL);
  }

  @Test
  void givenAllFields_whenBuild_thenHandleCreatedWithCorrectValues() {
    JetStreamListenerDetails listener = fullBuilder().build();

    assertThat(listener.getBean()).isSameAs(BEAN);
    assertThat(listener.getMethod()).isSameAs(METHOD);
    assertThat(listener.getSubject()).isEqualTo("orders.>");
    assertThat(listener.getStream()).isEqualTo("ORDERS");
    assertThat(listener.getDurable()).isEqualTo("order-processor");
    assertThat(listener.getQueue()).isEqualTo("");
    assertThat(listener.getConsumerType()).isEqualTo(ConsumerType.PUSH);
    assertThat(listener.getAckMode()).isEqualTo(AckMode.AUTO);
    assertThat(listener.getDeliverPolicy()).isEqualTo(DeliverPolicyType.ALL);
  }

  @Test
  void givenMissingBean_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withBean(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("bean is required");
  }

  @Test
  void givenMissingMethod_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withMethod(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("method is required");
  }

  @Test
  void givenMissingSubject_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withSubject(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("subject is required");
  }

  @Test
  void givenMissingStream_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withStream(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("stream is required");
  }

  @Test
  void givenMissingDurable_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withDurable(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("durable is required");
  }

  @Test
  void givenMissingQueue_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withQueue(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("queue is required");
  }

  @Test
  void givenMissingConsumerType_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withConsumerType(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("consumerType is required");
  }

  @Test
  void givenMissingAckMode_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withAckMode(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("ackMode is required");
  }

  @Test
  void givenMissingDeliverPolicy_whenBuild_thenThrowsIllegalStateException() {
    assertThatThrownBy(() -> fullBuilder().withDeliverPolicy(null).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("deliverPolicy is required");
  }
}
