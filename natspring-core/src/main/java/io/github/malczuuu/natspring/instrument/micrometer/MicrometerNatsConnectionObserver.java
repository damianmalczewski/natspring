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

import io.github.malczuuu.natspring.instrument.NatsConnectionObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.nats.client.ConnectionListener;
import java.util.Locale;

/**
 * {@link NatsConnectionObserver} that increments a {@code nats.connection.events} counter tagged
 * with the event name on each connection state change.
 *
 * <p>Register as a Spring bean; {@link #bindTo(MeterRegistry)} will be called automatically.
 *
 * @since 0.1.0
 */
public class MicrometerNatsConnectionObserver implements NatsConnectionObserver, MeterBinder {

  private MeterRegistry meterRegistry;

  /**
   * Creates an instance using a temporary {@link SimpleMeterRegistry} until {@link #bindTo} is
   * called.
   *
   * @since 0.1.0
   */
  public MicrometerNatsConnectionObserver() {
    meterRegistry = new SimpleMeterRegistry();
  }

  /**
   * Called when the connection state changes.
   *
   * @param event the connection event
   * @since 0.1.0
   */
  @Override
  public void onConnectionEvent(ConnectionListener.Events event) {
    Tags tags = Tags.of(Tag.of("event", event.name().toLowerCase(Locale.ROOT)));
    meterRegistry.counter("nats.connection.events", tags).increment();
  }

  /**
   * Called when the NATS server sends an error string.
   *
   * @param error the error text
   * @since 0.1.0
   */
  @Override
  public void onError(String error) {
    Tags tags = Tags.of("error", error);
    meterRegistry.counter("nats.connection.errors", tags).increment();
  }

  /**
   * Called when the client encounters an exception during processing.
   *
   * @param exception the exception
   * @since 0.1.0
   */
  @Override
  public void onException(Exception exception) {
    Tags tags = Tags.of("exception", exception.getClass().getSimpleName());
    meterRegistry.counter("nats.connection.exceptions", tags).increment();
  }

  /**
   * Called when a slow consumer is detected on the connection.
   *
   * @since 0.1.0
   */
  @Override
  public void onSlowConsumerDetected() {
    meterRegistry.counter("nats.connection.slow.consumer.detected").increment();
  }

  /**
   * Called when a message is discarded due to a full consumer queue.
   *
   * @since 0.1.0
   */
  @Override
  public void onMessageDiscarded() {
    meterRegistry.counter("nats.connection.message.discarded").increment();
  }

  /**
   * Replaces the temporary registry with the application-wide {@code registry}.
   *
   * @param registry the application-wide MeterRegistry to bind to
   * @since 0.1.0
   */
  @Override
  public void bindTo(MeterRegistry registry) {
    meterRegistry = registry;
  }
}
