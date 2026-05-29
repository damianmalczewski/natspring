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

package io.github.malczuuu.natspring.namastack.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for NATS outbox integration.
 *
 * @since 0.3.0
 */
@ConfigurationProperties(prefix = "natspring.namastack.outbox")
public class NatsOutboxProperties {

  /** Whether NATS outbox integration is enabled. */
  private boolean enabled = true;

  /** Default NATS subject for outbox events. */
  private String defaultSubject = "outbox-events";

  /**
   * Returns whether NATS outbox integration is enabled.
   *
   * @return whether NATS outbox integration is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether NATS outbox integration is enabled.
   *
   * @param enabled whether to enable NATS outbox integration
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the default NATS subject for outbox events.
   *
   * @return the default NATS subject
   */
  public String getDefaultSubject() {
    return defaultSubject;
  }

  /**
   * Sets the default NATS subject for outbox events.
   *
   * @param defaultSubject the default NATS subject
   */
  public void setDefaultSubject(String defaultSubject) {
    this.defaultSubject = defaultSubject;
  }
}
