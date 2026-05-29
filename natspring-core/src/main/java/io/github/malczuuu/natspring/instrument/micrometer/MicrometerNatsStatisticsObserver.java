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

package io.github.malczuuu.natspring.instrument.micrometer;

import io.github.malczuuu.natspring.connection.ConnectionManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.nats.client.Statistics;
import java.util.function.Supplier;

/**
 * {@link MeterBinder} that exposes NATS connection statistics from {@link
 * io.nats.client.Statistics} as Micrometer gauges (e.g. message counts, byte counts, reconnects,
 * pings). All gauges are registered under the {@code nats.connection.*} namespace.
 *
 * <p>Register as a Spring bean; {@link #bindTo(MeterRegistry)} will be called automatically.
 *
 * @since 0.1.0
 */
public class MicrometerNatsStatisticsObserver implements MeterBinder {

  private final ConnectionManager connectionManager;

  /**
   * Creates a new instance of {@link MicrometerNatsStatisticsObserver} with the provided {@code
   * connectionManager}.
   *
   * @param connectionManager provides access to the live NATS connection and its statistics
   * @since 0.1.0
   */
  public MicrometerNatsStatisticsObserver(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /**
   * Replaces the temporary registry with the application-wide {@code registry}. Metric descriptions
   * come from Javadocs of respective getters.
   *
   * @param registry the application-wide MeterRegistry to bind to
   * @since 0.1.0
   */
  @Override
  public void bindTo(MeterRegistry registry) {
    wrapGauge(
        registry,
        "nats.connection.pings",
        "the total number of pings that have been sent from this connection",
        () -> getStatistics().getPings());
    wrapGauge(
        registry,
        "nats.connection.reconnects",
        "the total number of times this connection has tried to reconnect",
        () -> getStatistics().getReconnects());
    wrapGauge(
        registry,
        "nats.connection.dropped.count",
        "the total number of messages dropped by this connection across all slow consumers",
        () -> getStatistics().getDroppedCount());
    wrapGauge(
        registry,
        "nats.connection.oks",
        "the total number of op +OKs received by this connection",
        () -> getStatistics().getOKs());
    wrapGauge(
        registry,
        "nats.connection.errs",
        "the total number of op -ERRs received by this connection",
        () -> getStatistics().getErrs());
    wrapGauge(
        registry,
        "nats.connection.exceptions",
        "the total number of exceptions seen by this connection",
        () -> getStatistics().getExceptions());
    wrapGauge(
        registry,
        "nats.connection.requests.sent",
        "the total number of requests sent by this connection",
        () -> getStatistics().getRequestsSent());
    wrapGauge(
        registry,
        "nats.connection.replies.received",
        "the total number of replies received by this connection",
        () -> getStatistics().getRepliesReceived());
    wrapGauge(
        registry,
        "nats.connection.duplicate.replies.received",
        "the total number of duplicate replies received by this connection, only counted if advanced stats are enabled",
        () -> getStatistics().getDuplicateRepliesReceived());
    wrapGauge(
        registry,
        "nats.connection.orphan.replies.received",
        "the total number of orphan replies received by this connection, only counted if advanced stats are enabled",
        () -> getStatistics().getOrphanRepliesReceived());
    wrapGauge(
        registry,
        "nats.connection.in.msgs",
        "the total number of messages that have come in to this connection",
        () -> getStatistics().getInMsgs());
    wrapGauge(
        registry,
        "nats.connection.out.msgs",
        "the total number of messages that have gone out of this connection",
        () -> getStatistics().getOutMsgs());
    wrapGauge(
        registry,
        "nats.connection.in.bytes",
        "the total number of message bytes that have come in to this connection",
        () -> getStatistics().getInBytes());
    wrapGauge(
        registry,
        "nats.connection.out.bytes",
        "the total number of message bytes that have gone out of this connection",
        () -> getStatistics().getOutBytes());
    wrapGauge(
        registry,
        "nats.connection.flush.counter",
        "the total number of outgoing message flushes by this connection",
        () -> getStatistics().getFlushCounter());
    wrapGauge(
        registry,
        "nats.connection.outstanding.requests",
        "the count of outstanding of requests from this connection",
        () -> getStatistics().getOutstandingRequests());
  }

  private <T extends Number> void wrapGauge(
      MeterRegistry registry, String name, String description, Supplier<T> supplier) {
    Gauge.builder(name, () -> getValueOrZero(supplier)).description(description).register(registry);
  }

  private <T extends Number> Number getValueOrZero(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      return 0;
    }
  }

  private Statistics getStatistics() {
    return connectionManager.getConnection().getStatistics();
  }
}
