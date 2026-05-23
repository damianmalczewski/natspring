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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicrometerNatsErrorObserverTests {

  private SimpleMeterRegistry registry;
  private MicrometerNatsErrorObserver observer;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    observer = new MicrometerNatsErrorObserver();
    observer.bindTo(registry);
  }

  @Test
  void givenServerError_whenOnError_thenErrorCounterIncrementedWithErrorTag() {
    observer.onError("-ERR 'Unknown Protocol Operation'");

    Counter counter =
        registry
            .find("nats.connection.errors")
            .tag("error", "-ERR 'Unknown Protocol Operation'")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenException_whenOnException_thenExceptionCounterIncrementedWithExceptionTag() {
    observer.onException(new IllegalStateException("connection dropped"));

    Counter counter =
        registry
            .find("nats.connection.exceptions")
            .tag("exception", "IllegalStateException")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenSlowConsumer_whenOnSlowConsumerDetected_thenSlowConsumerCounterIncremented() {
    observer.onSlowConsumerDetected();

    Counter counter = registry.find("nats.connection.slow.consumer.detected").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenDiscardedMessage_whenOnMessageDiscarded_thenDiscardedCounterIncremented() {
    observer.onMessageDiscarded();

    Counter counter = registry.find("nats.connection.message.discarded").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }
}
