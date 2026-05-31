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
import io.github.malczuuu.natspring.instrument.micrometer.MicrometerJetStreamListenerObserver;
import io.github.malczuuu.natspring.instrument.micrometer.MicrometerNatsConnectionObserver;
import io.github.malczuuu.natspring.instrument.micrometer.MicrometerNatsListenerObserver;
import io.github.malczuuu.natspring.instrument.micrometer.MicrometerNatsStatisticsObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.nats.client.Connection;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Spring Boot auto-configuration for NATS metrics instrumentation.
 *
 * <p>When Micrometer ({@code io.micrometer.core.instrument.MeterRegistry}) is on the classpath,
 * registers Micrometer-backed observer beans. Otherwise, falls back to no-op implementations. Any
 * observer bean declared in the application context takes precedence over these defaults.
 *
 * @since 0.1.0
 */
@AutoConfiguration
@ConditionalOnBooleanProperty(name = "nats.enabled", matchIfMissing = true)
@ConditionalOnClass(Connection.class)
public final class NatsMetricsAutoConfiguration {

  /** Creates a new {@link NatsMetricsAutoConfiguration}. */
  public NatsMetricsAutoConfiguration() {}

  @ConditionalOnClass(MeterRegistry.class)
  @Configuration(proxyBeanMethods = false)
  @Order(0)
  static final class MicrometerConfiguration {

    @Bean
    @ConditionalOnMissingBean(JetStreamListenerObserver.class)
    MicrometerJetStreamListenerObserver jetStreamListenerObserver() {
      return new MicrometerJetStreamListenerObserver();
    }

    @Bean
    @ConditionalOnMissingBean(NatsConnectionObserver.class)
    MicrometerNatsConnectionObserver natsConnectionObserver() {
      return new MicrometerNatsConnectionObserver();
    }

    @Bean
    @ConditionalOnMissingBean(NatsListenerObserver.class)
    MicrometerNatsListenerObserver natsListenerObserver() {
      return new MicrometerNatsListenerObserver();
    }

    @Bean
    @ConditionalOnMissingBean(MicrometerNatsStatisticsObserver.class)
    MicrometerNatsStatisticsObserver natsStatisticsObserver(BeanFactory beanFactory) {
      return new MicrometerNatsStatisticsObserver(beanFactory.getBean(Connection.class));
    }
  }

  @Configuration(proxyBeanMethods = false)
  @Order(1)
  static final class NoopConfiguration {

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
}
