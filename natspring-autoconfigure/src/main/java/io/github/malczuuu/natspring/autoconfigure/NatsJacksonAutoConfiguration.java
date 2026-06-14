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

import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.github.malczuuu.natspring.converter.jackson.JacksonNatsMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Boot auto-configuration for NATS Jackson integration.
 *
 * <p>When {@code jackson-databind} is on the classpath, registers a {@link
 * JacksonNatsMessageConverter} bean unless one is already defined.
 *
 * @since 0.4.0
 */
@AutoConfiguration(before = NatsAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "nats.enabled", matchIfMissing = true)
@ConditionalOnClass(JsonMapper.class)
public final class NatsJacksonAutoConfiguration {

  /** Creates a new {@link NatsJacksonAutoConfiguration}. */
  public NatsJacksonAutoConfiguration() {}

  @Bean
  @ConditionalOnMissingBean(NatsMessageConverter.class)
  JacksonNatsMessageConverter jacksonNatsMessageConverter(ObjectProvider<JsonMapper> jsonMapper) {
    return new JacksonNatsMessageConverter(
        jsonMapper.getIfAvailable(() -> JsonMapper.builder().findAndAddModules().build()));
  }
}
