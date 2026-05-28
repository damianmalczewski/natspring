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

import io.github.malczuuu.natsify.connection.ConnectionManager;
import io.github.malczuuu.natsify.connection.ConnectionOptionsBuilderCustomizer;
import io.github.malczuuu.natsify.connection.ConnectionOptionsFactory;
import io.github.malczuuu.natsify.connection.CustomizableOptionsFactory;
import io.github.malczuuu.natsify.connection.DefaultConnectionManager;
import io.github.malczuuu.natsify.connection.JetStreamConfigurer;
import io.github.malczuuu.natsify.connection.JetStreamManager;
import io.github.malczuuu.natsify.core.NatsMessageInterceptor;
import io.github.malczuuu.natsify.core.NatsOperations;
import io.github.malczuuu.natsify.core.NatsPublishInterceptor;
import io.github.malczuuu.natsify.core.NatsTemplate;
import io.github.malczuuu.natsify.core.NatsTemplateBuilderCustomizer;
import io.github.malczuuu.natsify.handler.JetStreamListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natsify.handler.JetStreamListenerEndpointRegistry;
import io.github.malczuuu.natsify.handler.JetStreamMessageListenerContainer;
import io.github.malczuuu.natsify.handler.MessageArgumentResolver;
import io.github.malczuuu.natsify.handler.NatsListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natsify.handler.NatsListenerEndpointRegistry;
import io.github.malczuuu.natsify.handler.NatsMessageListenerContainer;
import io.github.malczuuu.natsify.handler.SimpleMessageArgumentResolver;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.github.malczuuu.natsify.instrument.NatsConnectionObserver;
import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.api.StreamConfiguration;
import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Boot auto-configuration for core NATS infrastructure.
 *
 * @since 0.1.0
 */
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
  @ConditionalOnMissingBean(ConnectionOptionsFactory.class)
  CustomizableOptionsFactory customizableOptionsFactory(
      Environment environment,
      NatsProperties properties,
      NatsConnectionDetails connectionDetails,
      List<ConnectionOptionsBuilderCustomizer> customizers) {
    CustomizableOptionsFactory connectionOptionsFactory = new CustomizableOptionsFactory();
    connectionOptionsFactory.registerBuilderCustomizer(
        it -> {
          String connectionName = properties.getConnectionName();
          if (connectionName == null) {
            connectionName = environment.getProperty("spring.application.name");
          }

          if (StringUtils.hasLength(connectionDetails.getUsername())) {
            it = it.userInfo(connectionDetails.getUsername(), connectionDetails.getPassword());
          }

          if (properties.isNoEcho()) {
            it = it.noEcho();
          }
          if (properties.isNoRandomize()) {
            it = it.noRandomize();
          }
          if (properties.getInboxPrefix() != null) {
            it = it.inboxPrefix(properties.getInboxPrefix());
          }

          return it.server(connectionDetails.getServer())
              .connectionName(connectionName)
              .connectionTimeout(properties.getConnectionTimeout())
              .socketWriteTimeout(properties.getSocketWriteTimeout())
              .maxReconnects(properties.getMaxReconnects())
              .reconnectWait(properties.getReconnectWait())
              .reconnectJitter(properties.getReconnectJitter())
              .reconnectJitterTls(properties.getReconnectJitterTls())
              .reconnectBufferSize(properties.getReconnectBufferSize())
              .pingInterval(properties.getPingInterval())
              .maxPingsOut(properties.getMaxPingsOut())
              .requestCleanupInterval(properties.getRequestCleanupInterval());
        });
    customizers.forEach(connectionOptionsFactory::registerBuilderCustomizer);
    return connectionOptionsFactory;
  }

  @Bean
  @ConditionalOnMissingBean(ConnectionManager.class)
  DefaultConnectionManager natsDefaultConnectionManager(
      NatsProperties properties,
      ConnectionOptionsFactory connectionOptionsFactory,
      NatsListenerEndpointRegistry natsListenerEndpointRegistry,
      JetStreamListenerEndpointRegistry jetStreamListenerEndpointRegistry,
      NatsListenerObserver natsListenerObserver,
      JetStreamListenerObserver jetStreamListenerObserver,
      NatsConnectionObserver natsConnectionObserver,
      List<NatsMessageInterceptor> natsMessageInterceptors,
      JsonMapper jsonMapper) {
    MessageArgumentResolver argumentResolver = new SimpleMessageArgumentResolver(jsonMapper);
    return new DefaultConnectionManager(
        connectionOptionsFactory.getOptions(),
        List.of(
            new NatsMessageListenerContainer(
                natsListenerEndpointRegistry,
                argumentResolver,
                natsListenerObserver,
                natsMessageInterceptors),
            new JetStreamMessageListenerContainer(
                jetStreamListenerEndpointRegistry,
                argumentResolver,
                jetStreamListenerObserver,
                properties.getPullFetchBatchSize(),
                properties.getPullFetchTimeout(),
                natsMessageInterceptors)),
        natsConnectionObserver);
  }

  @Bean
  @ConditionalOnMissingBean(NatsTemplate.Builder.class)
  NatsTemplate.Builder natsTemplateBuilder(
      ConnectionManager connectionManager,
      JsonMapper jsonMapper,
      List<NatsPublishInterceptor> natsPublishInterceptors,
      List<NatsTemplateBuilderCustomizer> customizers) {
    NatsTemplate.Builder builder =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionManager)
            .withJsonMapper(jsonMapper)
            .addInterceptors(natsPublishInterceptors);
    for (NatsTemplateBuilderCustomizer customizer : customizers) {
      builder = customizer.customize(builder);
    }
    return builder;
  }

  @Bean
  @ConditionalOnMissingBean(NatsOperations.class)
  NatsTemplate natsTemplate(NatsTemplate.Builder builder) {
    return builder.build();
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamManager.class)
  JetStreamConfigurer jetStreamConfigurer(
      NatsProperties properties,
      ConnectionOptionsFactory connectionOptionsFactory,
      List<StreamConfiguration> streamConfigurations) {
    return new JetStreamConfigurer(
        connectionOptionsFactory.getOptions(),
        properties.isAutoStreamCreation(),
        streamConfigurations);
  }

  @Bean
  @ConditionalOnMissingBean(NatsListenerEndpointRegistry.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static NatsListenerEndpointRegistry natsListenerEndpointRegistry() {
    return new NatsListenerEndpointRegistry();
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamListenerEndpointRegistry.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static JetStreamListenerEndpointRegistry jetStreamListenerEndpointRegistry() {
    return new JetStreamListenerEndpointRegistry();
  }

  @Bean
  @ConditionalOnMissingBean(NatsListenerAnnotationBeanPostProcessor.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static NatsListenerAnnotationBeanPostProcessor natsListenerAnnotationBeanPostProcessor(
      NatsListenerEndpointRegistry natsListenerEndpointRegistry) {
    return new NatsListenerAnnotationBeanPostProcessor(natsListenerEndpointRegistry);
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamListenerAnnotationBeanPostProcessor.class)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static JetStreamListenerAnnotationBeanPostProcessor jetStreamListenerAnnotationBeanPostProcessor(
      JetStreamListenerEndpointRegistry jetStreamListenerEndpointRegistry) {
    return new JetStreamListenerAnnotationBeanPostProcessor(jetStreamListenerEndpointRegistry);
  }
}
