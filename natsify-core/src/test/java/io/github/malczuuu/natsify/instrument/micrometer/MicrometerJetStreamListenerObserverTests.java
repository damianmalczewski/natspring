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

package io.github.malczuuu.natsify.instrument.micrometer;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicrometerJetStreamListenerObserverTests {

  private SimpleMeterRegistry registry;
  private MicrometerJetStreamListenerObserver observer;

  @BeforeEach
  void beforeEach() {
    registry = new SimpleMeterRegistry();
    observer = new MicrometerJetStreamListenerObserver();
    observer.bindTo(registry);
  }

  @Test
  void givenMessage_whenReceived_thenReceivedCounterIncremented() {
    observer.onReceived("orders.>", "ORDERS");

    Counter counter =
        registry
            .find("nats.jetstream.messages.received")
            .tag("subject", "orders.>")
            .tag("stream", "ORDERS")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenAcked_thenAckedCounterIncremented() {
    observer.onAcked("orders.>", "ORDERS");

    Counter counter =
        registry
            .find("nats.jetstream.messages.acked")
            .tag("subject", "orders.>")
            .tag("stream", "ORDERS")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenNacked_thenNackedCounterIncremented() {
    observer.onNacked("orders.>", "ORDERS");

    Counter counter =
        registry
            .find("nats.jetstream.messages.nacked")
            .tag("subject", "orders.>")
            .tag("stream", "ORDERS")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenTerminated_thenTerminatedCounterIncrementedWithExceptionTag() {
    observer.onTerminated("orders.>", "ORDERS", new IllegalArgumentException("bad payload"));

    Counter counter =
        registry
            .find("nats.jetstream.messages.terminated")
            .tag("subject", "orders.>")
            .tag("stream", "ORDERS")
            .tag("exception", "IllegalArgumentException")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenTerminatedWithNullException_thenTerminatedCounterTaggedAsUnknown() {
    observer.onTerminated("orders.>", "ORDERS", null);

    Counter counter =
        registry.find("nats.jetstream.messages.terminated").tag("exception", "unknown").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenProcessed_thenDurationTimerRecorded() {
    observer.onProcessed("orders.>", "ORDERS", TimeUnit.MILLISECONDS.toNanos(5));

    Timer timer =
        registry
            .find("nats.jetstream.messages.duration")
            .tag("subject", "orders.>")
            .tag("stream", "ORDERS")
            .timer();
    assertThat(timer).isNotNull();
    assertThat(Objects.requireNonNull(timer).count()).isEqualTo(1L);
  }
}
