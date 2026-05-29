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

  private JetStreamListenerEndpoint.Builder fullBuilder() {
    return JetStreamListenerEndpoint.builder()
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
    JetStreamListenerEndpoint endpoint = fullBuilder().build();

    assertThat(endpoint.getBean()).isSameAs(BEAN);
    assertThat(endpoint.getMethod()).isSameAs(METHOD);
    assertThat(endpoint.getSubject()).isEqualTo("orders.>");
    assertThat(endpoint.getStream()).isEqualTo("ORDERS");
    assertThat(endpoint.getDurable()).isEqualTo("order-processor");
    assertThat(endpoint.getQueue()).isEqualTo("");
    assertThat(endpoint.getConsumerType()).isEqualTo(ConsumerType.PUSH);
    assertThat(endpoint.getAckMode()).isEqualTo(AckMode.AUTO);
    assertThat(endpoint.getDeliverPolicy()).isEqualTo(DeliverPolicyType.ALL);
  }

  @Test
  void givenMissingBean_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withBean(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("bean is required");
  }

  @Test
  void givenMissingMethod_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withMethod(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("method is required");
  }

  @Test
  void givenMissingSubject_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withSubject(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("subject is required");
  }

  @Test
  void givenMissingStream_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withStream(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("stream is required");
  }

  @Test
  void givenMissingDurable_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withDurable(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("durable is required");
  }

  @Test
  void givenMissingQueue_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withQueue(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("queue is required");
  }

  @Test
  void givenMissingConsumerType_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withConsumerType(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("consumerType is required");
  }

  @Test
  void givenMissingAckMode_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withAckMode(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ackMode is required");
  }

  @Test
  void givenMissingDeliverPolicy_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withDeliverPolicy(null).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("deliverPolicy is required");
  }

  @Test
  void givenPullConsumerWithQueue_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () -> fullBuilder().withConsumerType(ConsumerType.PULL).withQueue("some-queue").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("queue group is not supported for pull consumers");
  }

  @Test
  void givenDlqWithoutMaxDeliveries_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withDeadLetterSubject("dlq.subject").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxDeliveries must be positive when deadLetterSubject is set");
  }

  @Test
  void givenMaxDeliveriesWithoutDlq_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> fullBuilder().withMaxDeliveries(3).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("deadLetterSubject is required when maxDeliveries is set");
  }

  @Test
  void givenDlqWithManualAckMode_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                fullBuilder()
                    .withDeadLetterSubject("dlq.subject")
                    .withMaxDeliveries(3)
                    .withAckMode(AckMode.MANUAL)
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("deadLetterSubject is not supported with MANUAL ack mode");
  }
}
