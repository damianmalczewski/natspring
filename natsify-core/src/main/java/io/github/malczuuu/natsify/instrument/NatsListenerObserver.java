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

package io.github.malczuuu.natsify.instrument;

/** Observes core NATS listener invocation events. */
public interface NatsListenerObserver {

  /**
   * Returns a no-op implementation that discards all events.
   *
   * @return a no-op {@link NatsListenerObserver}
   */
  static NatsListenerObserver noop() {
    return new NatsListenerObserver() {};
  }

  /**
   * Called when a message is received before the handler is invoked.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  default void onReceived(String subject, String queue) {}

  /**
   * Called when the handler returns without throwing.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  default void onSucceeded(String subject, String queue) {}

  /**
   * Called when the handler throws an exception.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  default void onFailed(String subject, String queue) {}

  /**
   * Called when a message is dead-lettered after a failure.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   */
  default void onDeadLettered(String subject, String queue) {}

  /**
   * Called after every invocation (success or failure) with the total processing duration.
   *
   * @param subject the NATS subject
   * @param queue the queue group name, or empty string if not in a queue group
   * @param durationNanos elapsed time in nanoseconds from message receipt to handler completion
   */
  default void onProcessed(String subject, String queue, long durationNanos) {}
}
