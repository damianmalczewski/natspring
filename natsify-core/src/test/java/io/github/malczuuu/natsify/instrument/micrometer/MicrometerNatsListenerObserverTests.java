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

class MicrometerNatsListenerObserverTests {

  private SimpleMeterRegistry registry;
  private MicrometerNatsListenerObserver observer;

  @BeforeEach
  void beforeEach() {
    registry = new SimpleMeterRegistry();
    observer = new MicrometerNatsListenerObserver();
    observer.bindTo(registry);
  }

  @Test
  void givenMessage_whenReceived_thenReceivedCounterIncremented() {
    observer.onReceived("orders.placed", "");

    Counter counter =
        registry.find("nats.listener.messages.received").tag("subject", "orders.placed").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenSucceeded_thenSuccessCounterIncremented() {
    observer.onSucceeded("orders.placed", "");

    Counter counter =
        registry.find("nats.listener.messages.success").tag("subject", "orders.placed").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenFailed_thenErrorCounterIncremented() {
    observer.onFailed("orders.placed", "");

    Counter counter =
        registry.find("nats.listener.messages.error").tag("subject", "orders.placed").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMessage_whenProcessed_thenDurationTimerRecorded() {
    observer.onProcessed("orders.placed", "", TimeUnit.MILLISECONDS.toNanos(10));

    Timer timer =
        registry.find("nats.listener.messages.duration").tag("subject", "orders.placed").timer();
    assertThat(timer).isNotNull();
    assertThat(Objects.requireNonNull(timer).count()).isEqualTo(1L);
  }

  @Test
  void givenQueueGroup_whenReceived_thenCounterTaggedWithQueue() {
    observer.onReceived("orders.placed", "processors");

    Counter counter =
        registry
            .find("nats.listener.messages.received")
            .tag("subject", "orders.placed")
            .tag("queue", "processors")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }
}
