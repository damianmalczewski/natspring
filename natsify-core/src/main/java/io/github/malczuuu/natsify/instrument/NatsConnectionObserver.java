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

import io.nats.client.ConnectionListener;

/**
 * Observes NATS connection lifecycle events.
 *
 * @since 0.1.0
 */
public interface NatsConnectionObserver {

  /**
   * Returns a no-op implementation that discards all events.
   *
   * @return a no-op {@link NatsConnectionObserver}
   * @since 0.1.0
   */
  static NatsConnectionObserver noop() {
    return new NatsConnectionObserver() {};
  }

  /**
   * Called when the connection state changes.
   *
   * @param event the connection event
   * @since 0.1.0
   */
  default void onConnectionEvent(ConnectionListener.Events event) {}

  /**
   * Called when the NATS server sends an error string.
   *
   * @param error the error text
   * @since 0.1.0
   */
  default void onError(String error) {}

  /**
   * Called when the client encounters an exception during processing.
   *
   * @param exception the exception
   * @since 0.1.0
   */
  default void onException(Exception exception) {}

  /**
   * Called when a slow consumer is detected on the connection.
   *
   * @since 0.1.0
   */
  default void onSlowConsumerDetected() {}

  /**
   * Called when a message is discarded due to a full consumer queue.
   *
   * @since 0.1.0
   */
  default void onMessageDiscarded() {}
}
