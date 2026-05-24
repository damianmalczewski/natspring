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

package io.github.malczuuu.natsify.core;

/** Thrown when JetStream streams fail to be created or configured on startup. */
public class StreamConfigureException extends NatsIntegrationException {

  /** Creates a new {@link StreamConfigureException}. */
  public StreamConfigureException() {
    super();
  }

  /**
   * Creates a new {@link StreamConfigureException}.
   *
   * @param message the detail message
   */
  public StreamConfigureException(String message) {
    super(message);
  }

  /**
   * Creates a new {@link StreamConfigureException}.
   *
   * @param message the detail message
   * @param cause the underlying cause
   */
  public StreamConfigureException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new {@link StreamConfigureException}.
   *
   * @param cause the underlying cause
   */
  public StreamConfigureException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new {@link StreamConfigureException}.
   *
   * @param message the detail message
   * @param cause the underlying cause
   * @param enableSuppression whether suppression is enabled or disabled
   * @param writableStackTrace whether the stack trace should be writable
   */
  protected StreamConfigureException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
