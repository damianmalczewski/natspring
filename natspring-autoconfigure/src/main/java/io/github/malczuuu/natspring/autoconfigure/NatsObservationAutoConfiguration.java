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

import io.github.malczuuu.natspring.instrument.JetStreamListenerObserver;
import io.github.malczuuu.natspring.instrument.NatsConnectionObserver;
import io.github.malczuuu.natspring.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for NATS no-op metrics observer instrumentation.
 *
 * @since 0.4.0
 */
@AutoConfiguration(before = NatsAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "nats.enabled", matchIfMissing = true)
@ConditionalOnClass(Connection.class)
public final class NatsObservationAutoConfiguration {

  /** Creates a new {@link NatsObservationAutoConfiguration}. */
  public NatsObservationAutoConfiguration() {}

  @Bean
  @ConditionalOnMissingBean(JetStreamListenerObserver.class)
  JetStreamListenerObserver jetStreamListenerObserver() {
    return JetStreamListenerObserver.noop();
  }

  @Bean
  @ConditionalOnMissingBean(NatsConnectionObserver.class)
  NatsConnectionObserver natsConnectionObserver() {
    return NatsConnectionObserver.noop();
  }

  @Bean
  @ConditionalOnMissingBean(NatsListenerObserver.class)
  NatsListenerObserver natsListenerObserver() {
    return NatsListenerObserver.noop();
  }
}
