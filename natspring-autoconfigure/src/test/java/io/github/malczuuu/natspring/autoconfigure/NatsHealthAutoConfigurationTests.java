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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.malczuuu.natspring.connection.ConnectionLifecycle;
import io.github.malczuuu.natspring.health.NatsHealthIndicator;
import io.nats.client.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NatsHealthAutoConfigurationTests {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(NatsHealthAutoConfiguration.class);

  @Test
  void givenConnectionManagerBeanWithConnectedStatus_whenContextLoads_thenHealthIsUp() {
    ConnectionLifecycle connection = mock(ConnectionLifecycle.class);
    when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);

    runner
        .withBean(ConnectionLifecycle.class, () -> connection)
        .run(
            ctx -> {
              NatsHealthIndicator indicator = ctx.getBean(NatsHealthIndicator.class);
              Health health = indicator.health();
              assertThat(health).isNotNull();
              assertThat(health.getStatus()).isEqualTo(Status.UP);
              assertThat(health.getDetails())
                  .containsEntry("connectionStatus", Connection.Status.CONNECTED);
            });
  }

  @Test
  void givenConnectionManagerBeanWithDisconnectedStatus_whenContextLoads_thenHealthIsDown() {
    ConnectionLifecycle connection = mock(ConnectionLifecycle.class);
    when(connection.getStatus()).thenReturn(Connection.Status.DISCONNECTED);

    runner
        .withBean(ConnectionLifecycle.class, () -> connection)
        .run(
            ctx -> {
              NatsHealthIndicator indicator = ctx.getBean(NatsHealthIndicator.class);
              Health health = indicator.health();
              assertThat(health).isNotNull();
              assertThat(health.getStatus()).isEqualTo(Status.DOWN);
              assertThat(health.getDetails())
                  .containsEntry("connectionStatus", Connection.Status.DISCONNECTED);
            });
  }

  @Test
  void givenConnectionManagerThatThrows_whenHealthCalled_thenHealthIsDown() {
    ConnectionLifecycle connection = mock(ConnectionLifecycle.class);
    when(connection.getStatus()).thenThrow(new RuntimeException("connection failed"));

    runner
        .withBean(ConnectionLifecycle.class, () -> connection)
        .run(
            ctx -> {
              NatsHealthIndicator indicator = ctx.getBean(NatsHealthIndicator.class);
              Health health = indicator.health();
              assertThat(health).isNotNull();
              assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            });
  }

  @Test
  void givenCustomNatsHealthIndicatorBean_whenContextLoads_thenAutoConfiguredBeanNotRegistered() {
    ConnectionLifecycle connection = mock(ConnectionLifecycle.class);
    when(connection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    NatsHealthIndicator customIndicator = new NatsHealthIndicator(connection);

    runner
        .withBean(ConnectionLifecycle.class, () -> connection)
        .withBean("customNatsHealthIndicator", NatsHealthIndicator.class, () -> customIndicator)
        .run(ctx -> assertThat(ctx.getBean(NatsHealthIndicator.class)).isSameAs(customIndicator));
  }
}
