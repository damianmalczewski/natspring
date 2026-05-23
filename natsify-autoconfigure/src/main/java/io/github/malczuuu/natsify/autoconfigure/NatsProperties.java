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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration properties for Natsify Project, bound under the {@code natsify} prefix. */
@ConfigurationProperties(prefix = "natsify")
public class NatsProperties {

  /** Whether NATS auto-configuration is enabled. Default: {@code true}. */
  private final boolean enabled;

  /** NATS server URL. Default: {@code nats://localhost:4222}. */
  private final String server;

  /** Username for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable final String username;

  /** Password for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable final String password;

  /**
   * Whether declared {@code StreamConfiguration} beans are used to create or update JetStream
   * streams on startup. Default: {@code false}.
   */
  private final boolean autoStreamCreation;

  /**
   * Creates a new {@code NatsProperties} instance. Intended for use by the Spring Boot
   * configuration binding mechanism; prefer injecting the bound bean over constructing directly.
   *
   * @param enabled whether auto-configuration is enabled
   * @param server the NATS server URL
   * @param username optional username for authentication
   * @param password optional password for authentication
   * @param autoStreamCreation whether JetStream streams should be created or updated on startup
   */
  public NatsProperties(
      @DefaultValue("true") boolean enabled,
      @DefaultValue("nats://localhost:4222") String server,
      @Nullable String username,
      @Nullable String password,
      @DefaultValue("false") boolean autoStreamCreation) {
    this.enabled = enabled;
    this.server = server;
    this.username = username;
    this.password = password;
    this.autoStreamCreation = autoStreamCreation;
  }

  /**
   * Returns whether NATS auto-configuration is enabled.
   *
   * @return whether NATS auto-configuration is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the NATS server URL.
   *
   * @return the NATS server URL
   */
  public String getServer() {
    return server;
  }

  /**
   * Returns the username for authentication, or {@code null}.
   *
   * @return the username for authentication, or {@code null}
   */
  public @Nullable String getUsername() {
    return username;
  }

  /**
   * Returns the password for authentication, or {@code null}.
   *
   * @return the password for authentication, or {@code null}
   */
  public @Nullable String getPassword() {
    return password;
  }

  /**
   * Returns whether JetStream stream auto-creation is enabled.
   *
   * @return whether JetStream stream auto-creation is enabled
   */
  public boolean isAutoStreamCreation() {
    return autoStreamCreation;
  }
}
