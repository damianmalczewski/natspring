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

import io.github.malczuuu.natsify.connection.ConnectionConfigurer;
import io.github.malczuuu.natsify.connection.ConnectionManager;
import io.github.malczuuu.natsify.connection.ConnectionOptionsBuilderCustomizer;
import io.github.malczuuu.natsify.connection.JetStreamConfigurer;
import io.github.malczuuu.natsify.connection.JetStreamManager;
import io.github.malczuuu.natsify.core.NatsOperations;
import io.github.malczuuu.natsify.core.NatsTemplate;
import io.github.malczuuu.natsify.handler.JetStreamListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natsify.handler.JetStreamListenerRegistry;
import io.github.malczuuu.natsify.handler.NatsListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natsify.handler.NatsListenerRegistry;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.github.malczuuu.natsify.instrument.NatsConnectionObserver;
import io.github.malczuuu.natsify.instrument.NatsErrorObserver;
import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.api.StreamConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/** Spring Boot auto-configuration for core NATS infrastructure. */
@AutoConfiguration
@ConditionalOnBooleanProperty(name = "natsify.enabled", matchIfMissing = true)
@ConditionalOnClass(Connection.class)
@EnableConfigurationProperties(NatsProperties.class)
public final class NatsAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(NatsConnectionDetails.class)
  PropertiesNatsConnectionDetails propertiesNatsConnectionDetails(NatsProperties properties) {
    return new PropertiesNatsConnectionDetails(properties);
  }

  @Bean
  ConnectionOptionsBuilderCustomizer natsConnectionDetailsConnectionOptionsBuilderCustomizer(
      NatsConnectionDetails natsConnectionDetails) {
    return builder -> {
      builder = builder.server(natsConnectionDetails.getServer());

      if (StringUtils.hasLength(natsConnectionDetails.getUsername())) {
        builder =
            builder.userInfo(
                natsConnectionDetails.getUsername(), natsConnectionDetails.getPassword());
      }
      return builder;
    };
  }

  @Bean
  @ConditionalOnMissingBean(ConnectionManager.class)
  ConnectionConfigurer natsConnectionConfigurer(
      ObjectProvider<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizerProvider,
      NatsListenerRegistry natsListenerRegistry,
      JetStreamListenerRegistry jetStreamListenerRegistry,
      NatsListenerObserver natsListenerObserver,
      JetStreamListenerObserver jetStreamListenerObserver,
      NatsConnectionObserver natsConnectionObserver,
      NatsErrorObserver natsErrorObserver,
      JsonMapper jsonMapper) {
    return new ConnectionConfigurer(
        connectionOptionsBuilderCustomizerProvider.orderedStream().toList(),
        natsListenerRegistry,
        jetStreamListenerRegistry,
        jsonMapper,
        natsListenerObserver,
        jetStreamListenerObserver,
        natsConnectionObserver,
        natsErrorObserver);
  }

  @Bean
  @ConditionalOnMissingBean(NatsOperations.class)
  NatsTemplate natsTemplate(ConnectionManager connectionManager, JsonMapper jsonMapper) {
    return new NatsTemplate(connectionManager, jsonMapper);
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamManager.class)
  @ConditionalOnBooleanProperty(name = "natsify.auto-stream-creation")
  JetStreamConfigurer jetStreamConfigurer(
      ObjectProvider<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizerProvider,
      ObjectProvider<StreamConfiguration> streamConfigurationProvider) {
    return new JetStreamConfigurer(
        connectionOptionsBuilderCustomizerProvider.orderedStream().toList(),
        streamConfigurationProvider.orderedStream().toList());
  }

  @Bean
  @ConditionalOnMissingBean(NatsListenerRegistry.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static NatsListenerRegistry natsListenerRegistry() {
    return new NatsListenerRegistry();
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamListenerRegistry.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static JetStreamListenerRegistry jetStreamListenerRegistry() {
    return new JetStreamListenerRegistry();
  }

  @Bean
  @ConditionalOnMissingBean(NatsListenerAnnotationBeanPostProcessor.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static NatsListenerAnnotationBeanPostProcessor natsListenerAnnotationBeanPostProcessor(
      NatsListenerRegistry natsListenerRegistry) {
    return new NatsListenerAnnotationBeanPostProcessor(natsListenerRegistry);
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamListenerAnnotationBeanPostProcessor.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static JetStreamListenerAnnotationBeanPostProcessor jetStreamListenerAnnotationBeanPostProcessor(
      JetStreamListenerRegistry jetStreamListenerRegistry) {
    return new JetStreamListenerAnnotationBeanPostProcessor(jetStreamListenerRegistry);
  }
}
