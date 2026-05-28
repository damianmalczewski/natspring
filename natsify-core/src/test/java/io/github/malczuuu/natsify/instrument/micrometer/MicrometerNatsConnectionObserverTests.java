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
import io.nats.client.ConnectionListener;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicrometerNatsConnectionObserverTests {

  private SimpleMeterRegistry registry;
  private MicrometerNatsConnectionObserver observer;

  @BeforeEach
  void beforeEach() {
    registry = new SimpleMeterRegistry();
    observer = new MicrometerNatsConnectionObserver();
    observer.bindTo(registry);
  }

  @Test
  void givenConnectionEvent_whenConnected_thenEventCounterIncrementedWithConnectedTag() {
    observer.onConnectionEvent(ConnectionListener.Events.CONNECTED);

    Counter counter = registry.find("nats.connection.events").tag("event", "connected").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenConnectionEvent_whenDisconnected_thenEventCounterIncrementedWithDisconnectedTag() {
    observer.onConnectionEvent(ConnectionListener.Events.DISCONNECTED);

    Counter counter =
        registry.find("nats.connection.events").tag("event", "disconnected").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenConnectionEvent_whenReconnected_thenEventCounterIncrementedWithReconnectedTag() {
    observer.onConnectionEvent(ConnectionListener.Events.RECONNECTED);

    Counter counter = registry.find("nats.connection.events").tag("event", "reconnected").counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(1.0);
  }

  @Test
  void givenMultipleEvents_whenEachOccurs_thenCountersAreIndependentlyTagged() {
    observer.onConnectionEvent(ConnectionListener.Events.CONNECTED);
    observer.onConnectionEvent(ConnectionListener.Events.CONNECTED);
    observer.onConnectionEvent(ConnectionListener.Events.DISCONNECTED);

    Counter connected = registry.find("nats.connection.events").tag("event", "connected").counter();
    Counter disconnected =
        registry.find("nats.connection.events").tag("event", "disconnected").counter();
    assertThat(Objects.requireNonNull(connected).count()).isEqualTo(2.0);
    assertThat(Objects.requireNonNull(disconnected).count()).isEqualTo(1.0);
  }
}
