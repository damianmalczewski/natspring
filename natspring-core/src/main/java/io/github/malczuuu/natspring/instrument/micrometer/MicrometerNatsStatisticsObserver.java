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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.nats.client.Connection;
import java.util.function.Supplier;

/**
 * {@link MeterBinder} that exposes NATS connection statistics from {@link
 * io.nats.client.Statistics} as Micrometer gauges (e.g. message counts, byte counts, reconnects,
 * pings). All gauges are registered under the {@code nats.statistics.*} namespace.
 *
 * <p>Register as a Spring bean; {@link #bindTo(MeterRegistry)} will be called automatically.
 *
 * @since 0.1.0
 */
public class MicrometerNatsStatisticsObserver implements MeterBinder {

  private final Connection connection;

  /**
   * Creates a new {@link MicrometerNatsStatisticsObserver}.
   *
   * @param connection NATS connection to retrieve statistics from
   */
  public MicrometerNatsStatisticsObserver(Connection connection) {
    this.connection = connection;
  }

  /**
   * Registers all NATS statistics gauges against the provided {@code registry}.
   *
   * @param registry the MeterRegistry to bind to
   */
  @Override
  public void bindTo(MeterRegistry registry) {
    wrapGauge(
        registry,
        "nats.statistics.pings",
        "the total number of pings that have been sent from this connection",
        () -> connection.getStatistics().getPings());
    wrapGauge(
        registry,
        "nats.statistics.reconnects",
        "the total number of times this connection has tried to reconnect",
        () -> connection.getStatistics().getReconnects());
    wrapGauge(
        registry,
        "nats.statistics.dropped.count",
        "the total number of messages dropped by this connection across all slow consumers",
        () -> connection.getStatistics().getDroppedCount());
    wrapGauge(
        registry,
        "nats.statistics.oks",
        "the total number of op +OKs received by this connection",
        () -> connection.getStatistics().getOKs());
    wrapGauge(
        registry,
        "nats.statistics.errs",
        "the total number of op -ERRs received by this connection",
        () -> connection.getStatistics().getErrs());
    wrapGauge(
        registry,
        "nats.statistics.exceptions",
        "the total number of exceptions seen by this connection",
        () -> connection.getStatistics().getExceptions());
    wrapGauge(
        registry,
        "nats.statistics.requests.sent",
        "the total number of requests sent by this connection",
        () -> connection.getStatistics().getRequestsSent());
    wrapGauge(
        registry,
        "nats.statistics.replies.received",
        "the total number of replies received by this connection",
        () -> connection.getStatistics().getRepliesReceived());
    wrapGauge(
        registry,
        "nats.statistics.duplicate.replies.received",
        "the total number of duplicate replies received by this connection, only counted if advanced stats are enabled",
        () -> connection.getStatistics().getDuplicateRepliesReceived());
    wrapGauge(
        registry,
        "nats.statistics.orphan.replies.received",
        "the total number of orphan replies received by this connection, only counted if advanced stats are enabled",
        () -> connection.getStatistics().getOrphanRepliesReceived());
    wrapGauge(
        registry,
        "nats.statistics.in.msgs",
        "the total number of messages that have come in to this connection",
        () -> connection.getStatistics().getInMsgs());
    wrapGauge(
        registry,
        "nats.statistics.out.msgs",
        "the total number of messages that have gone out of this connection",
        () -> connection.getStatistics().getOutMsgs());
    wrapGauge(
        registry,
        "nats.statistics.in.bytes",
        "the total number of message bytes that have come in to this connection",
        () -> connection.getStatistics().getInBytes());
    wrapGauge(
        registry,
        "nats.statistics.out.bytes",
        "the total number of message bytes that have gone out of this connection",
        () -> connection.getStatistics().getOutBytes());
    wrapGauge(
        registry,
        "nats.statistics.flush.counter",
        "the total number of outgoing message flushes by this connection",
        () -> connection.getStatistics().getFlushCounter());
    wrapGauge(
        registry,
        "nats.statistics.outstanding.requests",
        "the count of outstanding of requests from this connection",
        () -> connection.getStatistics().getOutstandingRequests());
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
}
