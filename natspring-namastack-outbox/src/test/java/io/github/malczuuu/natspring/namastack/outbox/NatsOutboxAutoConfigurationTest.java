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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.namastack.outbox.autoconfigure.NatsOutboxAutoConfiguration;
import io.namastack.outbox.handler.OutboxRecordMetadata;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class NatsOutboxAutoConfigurationTest {

  private ApplicationContextRunner contextRunner() {
    return new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(NatsOutboxAutoConfiguration.class))
        .withUserConfiguration(ConfigWithNatsOperations.class);
  }

  @Nested
  class NatsOutboxRoutingBeanTests {

    @Test
    void givenNoCustomRouting_whenContextLoads_thenCreatesDefaultBean() {
      contextRunner().run(context -> assertThat(context).hasSingleBean(NatsOutboxRouting.class));
    }

    @Test
    void givenCustomRouting_whenContextLoads_thenUsesCustomBean() {
      contextRunner()
          .withUserConfiguration(ConfigWithCustomRouting.class)
          .run(
              context -> {
                assertThat(context).hasSingleBean(NatsOutboxRouting.class);
                NatsOutboxRouting routing = context.getBean(NatsOutboxRouting.class);
                assertThat(routing.resolveSubject("payload", createMetadata()))
                    .isEqualTo("custom-subject");
              });
    }

    @Test
    void givenCustomDefaultSubject_whenContextLoads_thenUsesConfiguredSubject() {
      contextRunner()
          .withPropertyValues("natspring.namastack.outbox.default-subject=my-custom-subject")
          .run(
              context -> {
                NatsOutboxRouting routing = context.getBean(NatsOutboxRouting.class);
                assertThat(routing.resolveSubject("payload", createMetadata()))
                    .isEqualTo("my-custom-subject");
              });
    }

    @Test
    void givenNoCustomDefaultSubject_whenContextLoads_thenUsesOutboxEventsDefault() {
      contextRunner()
          .run(
              context -> {
                NatsOutboxRouting routing = context.getBean(NatsOutboxRouting.class);
                assertThat(routing.resolveSubject("payload", createMetadata()))
                    .isEqualTo("outbox-events");
              });
    }
  }

  @Nested
  class NatsOutboxHandlerBeanTests {

    @Test
    void givenNatsOperationsBean_whenContextLoads_thenCreatesHandlerBean() {
      contextRunner().run(context -> assertThat(context).hasSingleBean(NatsOutboxHandler.class));
    }
  }

  @Nested
  class ConditionTests {

    @Test
    void givenDefault_whenContextLoads_thenConfigurationIsActive() {
      contextRunner()
          .run(
              context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(NatsOutboxRouting.class);
                assertThat(context).hasSingleBean(NatsOutboxHandler.class);
              });
    }

    @Test
    void givenEnabledFalse_whenContextLoads_thenConfigurationIsDisabled() {
      contextRunner()
          .withPropertyValues("natspring.namastack.outbox.enabled=false")
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(NatsOutboxRouting.class);
                assertThat(context).doesNotHaveBean(NatsOutboxHandler.class);
              });
    }

    @Test
    void givenEnabledTrue_whenContextLoads_thenConfigurationIsActive() {
      contextRunner()
          .withPropertyValues("natspring.namastack.outbox.enabled=true")
          .run(
              context -> {
                assertThat(context).hasSingleBean(NatsOutboxRouting.class);
                assertThat(context).hasSingleBean(NatsOutboxHandler.class);
              });
    }
  }

  private static OutboxRecordMetadata createMetadata() {
    return new OutboxRecordMetadata("test-key", "test-handler", Instant.now(), Map.of(), 0);
  }

  @Configuration
  static class ConfigWithNatsOperations {

    @Bean
    public NatsOperations natsOperations() {
      return mock(NatsOperations.class);
    }
  }

  @Configuration
  static class ConfigWithCustomRouting {

    @Bean
    public NatsOutboxRouting natsOutboxRouting() {
      return NatsOutboxRouting.builder().defaults(route -> route.target("custom-subject")).build();
    }
  }
}
