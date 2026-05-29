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

package io.github.malczuuu.natspring.core;

import java.io.Serial;

/**
 * Thrown when a NATS connection cannot be established.
 *
 * @since 0.1.0
 */
public class ConnectionException extends NatsIntegrationException {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link ConnectionException}.
   *
   * @since 0.1.0
   */
  public ConnectionException() {
    super();
  }

  /**
   * Creates a new {@link ConnectionException}.
   *
   * @param message the detail message
   * @since 0.1.0
   */
  public ConnectionException(String message) {
    super(message);
  }

  /**
   * Creates a new {@link ConnectionException}.
   *
   * @param message the detail message
   * @param cause the underlying cause
   * @since 0.1.0
   */
  public ConnectionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new {@link ConnectionException}.
   *
   * @param cause the underlying cause
   * @since 0.1.0
   */
  public ConnectionException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new {@link ConnectionException}.
   *
   * @param message the detail message
   * @param cause the underlying cause
   * @param enableSuppression whether suppression is enabled or disabled
   * @param writableStackTrace whether the stack trace should be writable
   * @since 0.1.0
   */
  protected ConnectionException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
