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

package io.github.malczuuu.natsify.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/** Connection details for establishing a NATS connection. */
public interface NatsConnectionDetails extends ConnectionDetails {

  /**
   * Returns the NATS server URL (e.g. {@code nats://localhost:4222}).
   *
   * @return the server URL
   */
  String getServer();

  /**
   * Returns the username for authentication, or {@code null} if not required.
   *
   * @return the username, or {@code null}
   */
  @Nullable String getUsername();

  /**
   * Returns the password for authentication, or {@code null} if not required.
   *
   * @return the password, or {@code null}
   */
  @Nullable String getPassword();
}
