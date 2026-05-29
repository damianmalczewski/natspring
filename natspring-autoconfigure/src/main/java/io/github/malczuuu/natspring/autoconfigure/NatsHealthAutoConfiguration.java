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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for NATS health indicators.
 *
 * <p>When {@code spring-boot-health} is on the classpath, registers a {@link NatsHealthIndicator}
 * bean unless one is already defined.
 *
 * @since 0.1.0
 */
@AutoConfiguration(after = NatsAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "nats.enabled", matchIfMissing = true)
@ConditionalOnClass({Connection.class, HealthIndicator.class})
public final class NatsHealthAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(NatsHealthIndicator.class)
  NatsHealthIndicator natsHealthIndicator(ConnectionManager connectionManager) {
    return new NatsHealthIndicator(connectionManager);
  }
}
