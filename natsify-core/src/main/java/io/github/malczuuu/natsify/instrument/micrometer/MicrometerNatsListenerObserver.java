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

import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;

/**
 * {@link NatsListenerObserver} that records core NATS listener metrics via Micrometer. Increments
 * counters {@code nats.listener.messages.received}, {@code nats.listener.messages.success}, and
 * {@code nats.listener.messages.error}, and records processing time to the {@code
 * nats.listener.messages.duration} timer - all tagged with {@code subject} and {@code queue}.
 *
 * <p>Register as a Spring bean; {@link #bindTo(MeterRegistry)} will be called automatically.
 */
public class MicrometerNatsListenerObserver implements NatsListenerObserver, MeterBinder {

  private MeterRegistry meterRegistry;

  /**
   * Creates an instance using a temporary {@link SimpleMeterRegistry} until {@link #bindTo} is
   * called.
   */
  public MicrometerNatsListenerObserver() {
    meterRegistry = new SimpleMeterRegistry();
  }

  /**
   * Called when a message is received before the handler is invoked.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  @Override
  public void onReceived(String subject, String queue) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("queue", queue));
    meterRegistry.counter("nats.listener.messages.received", tags).increment();
  }

  /**
   * Called when the handler returns without throwing.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  @Override
  public void onSucceeded(String subject, String queue) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("queue", queue));
    meterRegistry.counter("nats.listener.messages.success", tags).increment();
  }

  /**
   * Called when the handler throws an exception.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  @Override
  public void onFailed(String subject, String queue) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("queue", queue));
    meterRegistry.counter("nats.listener.messages.error", tags).increment();
  }

  /**
   * Called after every invocation (success or failure) with the total processing duration.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   * @param durationNanos elapsed time in nanoseconds from message receipt to handler completion
   */
  @Override
  public void onProcessed(String subject, String queue, long durationNanos) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("queue", queue));
    meterRegistry
        .timer("nats.listener.messages.duration", tags)
        .record(durationNanos, TimeUnit.NANOSECONDS);
  }

  /** Replaces the temporary registry with the application-wide {@code registry}. */
  @Override
  public void bindTo(MeterRegistry registry) {
    meterRegistry = registry;
  }
}
