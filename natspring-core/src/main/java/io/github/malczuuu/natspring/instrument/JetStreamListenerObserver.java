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

package io.github.malczuuu.natspring.instrument;

import org.jspecify.annotations.Nullable;

/**
 * Observes JetStream listener invocation and ack events.
 *
 * @since 0.1.0
 */
public interface JetStreamListenerObserver {

  /**
   * Returns a no-op implementation that discards all events.
   *
   * @return a no-op {@link JetStreamListenerObserver}
   * @since 0.1.0
   */
  static JetStreamListenerObserver noop() {
    return new JetStreamListenerObserver() {};
  }

  /**
   * Called when a JetStream message is received before the handler is invoked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  default void onReceived(String subject, String stream) {}

  /**
   * Called when the handler returns successfully and the message is acked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  default void onAcked(String subject, String stream) {}

  /**
   * Called when the handler throws and the message is nacked.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  default void onNacked(String subject, String stream) {}

  /**
   * Called when the message is terminated (e.g. deserialization failure).
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @param e the exception that caused termination, or {@code null} if not exception-driven
   * @since 0.1.0
   */
  default void onTerminated(String subject, String stream, @Nullable Exception e) {}

  /**
   * Called when a message is dead-lettered after exhausting delivery attempts.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @since 0.1.0
   */
  default void onDeadLettered(String subject, String stream) {}

  /**
   * Called after every invocation with the total processing duration.
   *
   * @param subject the message subject
   * @param stream the JetStream stream name
   * @param durationNanos elapsed time in nanoseconds from message receipt to handler completion
   * @since 0.1.0
   */
  default void onProcessed(String subject, String stream, long durationNanos) {}
}
