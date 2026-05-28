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
import static org.mockito.Mockito.when;

import io.github.malczuuu.natsify.connection.ConnectionManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.nats.client.Connection;
import io.nats.client.Statistics;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MicrometerNatsStatisticsObserverTests {

  private SimpleMeterRegistry registry;
  private Statistics statistics;

  @BeforeEach
  void beforeEach() {
    statistics = Mockito.mock(Statistics.class);
    Connection connection = Mockito.mock(Connection.class);
    ConnectionManager connectionManager = Mockito.mock(ConnectionManager.class);
    when(connection.getStatistics()).thenReturn(statistics);
    when(connectionManager.getConnection()).thenReturn(connection);

    registry = new SimpleMeterRegistry();
    new MicrometerNatsStatisticsObserver(connectionManager).bindTo(registry);
  }

  @Test
  void givenConnection_whenInMsgsRead_thenGaugeReflectsStatisticsValue() {
    when(statistics.getInMsgs()).thenReturn(42L);

    Gauge gauge = registry.find("nats.connection.in.msgs").gauge();
    assertThat(gauge).isNotNull();
    assertThat(Objects.requireNonNull(gauge).value()).isEqualTo(42.0);
  }

  @Test
  void givenConnection_whenOutMsgsRead_thenGaugeReflectsStatisticsValue() {
    when(statistics.getOutMsgs()).thenReturn(7L);

    Gauge gauge = registry.find("nats.connection.out.msgs").gauge();
    assertThat(gauge).isNotNull();
    assertThat(Objects.requireNonNull(gauge).value()).isEqualTo(7.0);
  }

  @Test
  void givenConnection_whenInBytesRead_thenGaugeReflectsStatisticsValue() {
    when(statistics.getInBytes()).thenReturn(1024L);

    Gauge gauge = registry.find("nats.connection.in.bytes").gauge();
    assertThat(gauge).isNotNull();
    assertThat(Objects.requireNonNull(gauge).value()).isEqualTo(1024.0);
  }

  @Test
  void givenConnection_whenOutBytesRead_thenGaugeReflectsStatisticsValue() {
    when(statistics.getOutBytes()).thenReturn(512L);

    Gauge gauge = registry.find("nats.connection.out.bytes").gauge();
    assertThat(gauge).isNotNull();
    assertThat(Objects.requireNonNull(gauge).value()).isEqualTo(512.0);
  }

  @Test
  void givenConnection_whenReconnectsRead_thenGaugeReflectsStatisticsValue() {
    when(statistics.getReconnects()).thenReturn(3L);

    Gauge gauge = registry.find("nats.connection.reconnects").gauge();
    assertThat(gauge).isNotNull();
    assertThat(Objects.requireNonNull(gauge).value()).isEqualTo(3.0);
  }

  @Test
  void givenAllGaugesRegistered_whenContextStarted_thenAllGaugesPresent() {
    assertThat(registry.find("nats.connection.pings").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.reconnects").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.dropped.count").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.oks").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.errs").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.exceptions").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.requests.sent").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.replies.received").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.duplicate.replies.received").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.orphan.replies.received").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.in.msgs").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.out.msgs").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.in.bytes").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.out.bytes").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.flush.counter").gauge()).isNotNull();
    assertThat(registry.find("nats.connection.outstanding.requests").gauge()).isNotNull();
  }
}
