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

import io.github.malczuuu.natsify.instrument.NatsConnectionObserver;
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
 */
public class MicrometerNatsConnectionObserver implements NatsConnectionObserver, MeterBinder {

  private MeterRegistry meterRegistry;

  /**
   * Creates an instance using a temporary {@link SimpleMeterRegistry} until {@link #bindTo} is
   * called.
   */
  public MicrometerNatsConnectionObserver() {
    meterRegistry = new SimpleMeterRegistry();
  }

  /**
   * Called when the connection state changes.
   *
   * @param event the connection event
   */
  @Override
  public void onConnectionEvent(ConnectionListener.Events event) {
    Tags tags = Tags.of(Tag.of("event", event.name().toLowerCase(Locale.ROOT)));
    meterRegistry.counter("nats.connection.events", tags).increment();
  }

  /** Replaces the temporary registry with the application-wide {@code registry}. */
  @Override
  public void bindTo(MeterRegistry registry) {
    meterRegistry = registry;
  }
}
