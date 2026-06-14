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

package io.github.malczuuu.natspring.namastack.outbox.autoconfigure;

import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.namastack.outbox.DefaultNatsOutboxHandler;
import io.github.malczuuu.natspring.namastack.outbox.NatsOutboxHandler;
import io.github.malczuuu.natspring.namastack.outbox.NatsOutboxProperties;
import io.github.malczuuu.natspring.namastack.outbox.NatsOutboxRouting;
import io.namastack.outbox.Outbox;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for NATS outbox integration.
 *
 * <p>Automatically configures {@link NatsOutboxHandler} when:
 *
 * <ul>
 *   <li>Natspring ({@link NatsOperations}) is on the classpath
 *   <li>A {@link NatsOperations} bean is available
 *   <li>Property {@code natspring.namastack.outbox.enabled} is {@code true} (default)
 * </ul>
 *
 * <p>Provide a custom {@link NatsOutboxRouting} bean to override the default routing behavior.
 *
 * <h2>Configuration Properties</h2>
 *
 * <pre>{@code
 * namastack:
 *   outbox:
 *     nats:
 *       enabled: true
 *       default-subject: my-events
 * }</pre>
 *
 * @since 0.3.0
 */
@AutoConfiguration
@ConditionalOnClass({NatsOperations.class, Outbox.class})
@ConditionalOnProperty(
    name = "natspring.namastack.outbox.enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(NatsOutboxProperties.class)
public final class NatsOutboxAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(NatsOutboxRouting.class)
  NatsOutboxRouting natsOutboxRouting(NatsOutboxProperties properties) {
    return NatsOutboxRouting.builder()
        .defaults(route -> route.target(properties.getDefaultSubject()))
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(NatsOutboxHandler.class)
  NatsOutboxHandler natsOutboxHandler(NatsOperations natsOperations, NatsOutboxRouting routing) {
    return new DefaultNatsOutboxHandler(natsOperations, routing);
  }
}
