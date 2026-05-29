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

package io.github.malczuuu.natspring.autoconfigure;

import io.github.malczuuu.natspring.connection.ConnectionManager;
import io.nats.client.Connection;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * {@link HealthIndicator} that reports the status of the NATS connection.
 *
 * <p>Reports {@code UP} when the connection status is {@link Connection.Status#CONNECTED} and
 * {@code DOWN} for all other statuses.
 *
 * @since 0.1.0
 */
public class NatsHealthIndicator implements HealthIndicator {

  private final ConnectionManager connectionManager;

  /**
   * Creates a new {@link NatsHealthIndicator}.
   *
   * @param connectionManager the connection manager used to obtain the active NATS connection
   * @since 0.1.0
   */
  public NatsHealthIndicator(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /**
   * Return an indication of health of NATS connection.
   *
   * @return the health of NATS connection
   * @since 0.1.0
   */
  @Override
  public Health health() {
    try {
      Connection connection = connectionManager.getConnection();
      Connection.Status status = connection.getStatus();
      if (status == Connection.Status.CONNECTED) {
        return Health.up().withDetail("connectionStatus", status).build();
      }
      return Health.down().withDetail("connectionStatus", status).build();
    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
