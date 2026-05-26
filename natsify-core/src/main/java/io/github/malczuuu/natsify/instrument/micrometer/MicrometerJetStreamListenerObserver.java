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

import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

/**
 * {@link JetStreamListenerObserver} that records JetStream listener metrics via Micrometer.
 * Increments counters {@code nats.jetstream.messages.received}, {@code
 * nats.jetstream.messages.acked}, {@code nats.jetstream.messages.nacked}, and {@code
 * nats.jetstream.messages.terminated}, and records processing time to the {@code
 * nats.jetstream.messages.duration} timer - all tagged with {@code subject} and {@code stream}.
 *
 * <p>Register as a Spring bean; {@link #bindTo(MeterRegistry)} will be called automatically.
 *
 * @since 0.1.0
 */
public class MicrometerJetStreamListenerObserver implements JetStreamListenerObserver, MeterBinder {

  private MeterRegistry meterRegistry;

  /**
   * Creates an instance using a temporary {@link SimpleMeterRegistry} until {@link #bindTo} is
   * called.
   *
   * @since 0.1.0
   */
  public MicrometerJetStreamListenerObserver() {
    this.meterRegistry = new SimpleMeterRegistry();
  }

  /**
   * Called when a JetStream message is received before the handler is invoked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  @Override
  public void onReceived(String subject, String stream) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("stream", stream));
    meterRegistry.counter("nats.jetstream.messages.received", tags).increment();
  }

  /**
   * Called when the handler returns successfully and the message is acked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  @Override
  public void onAcked(String subject, String stream) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("stream", stream));
    meterRegistry.counter("nats.jetstream.messages.acked", tags).increment();
  }

  /**
   * Called when the handler throws and the message is nacked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  @Override
  public void onNacked(String subject, String stream) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("stream", stream));
    meterRegistry.counter("nats.jetstream.messages.nacked", tags).increment();
  }

  /**
   * Called when the message is terminated (e.g. deserialization failure).
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @param e the exception that caused termination, or {@code null} if not exception-driven
   * @since 0.1.0
   */
  @Override
  public void onTerminated(String subject, String stream, @Nullable Exception e) {
    Tags tags =
        Tags.of(
            Tag.of("subject", subject),
            Tag.of("stream", stream),
            Tag.of("exception", e != null ? e.getClass().getSimpleName() : "unknown"));
    meterRegistry.counter("nats.jetstream.messages.terminated", tags).increment();
  }

  /**
   * Called when a message is dead-lettered after exhausting delivery attempts.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  @Override
  public void onDeadLettered(String subject, String stream) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("stream", stream));
    meterRegistry.counter("nats.jetstream.messages.deadlettered", tags).increment();
  }

  /**
   * Called after every invocation with the total processing duration.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @param durationNanos elapsed time in nanoseconds from message receipt to handler completion
   * @since 0.1.0
   */
  @Override
  public void onProcessed(String subject, String stream, long durationNanos) {
    Tags tags = Tags.of(Tag.of("subject", subject), Tag.of("stream", stream));
    meterRegistry
        .timer("nats.jetstream.messages.duration", tags)
        .record(durationNanos, TimeUnit.NANOSECONDS);
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
