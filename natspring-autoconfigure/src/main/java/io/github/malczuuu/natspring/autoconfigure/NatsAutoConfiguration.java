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

import io.github.malczuuu.natspring.connection.ConnectionLifecycle;
import io.github.malczuuu.natspring.connection.ConnectionOptionsBuilderCustomizer;
import io.github.malczuuu.natspring.connection.ConnectionOptionsFactory;
import io.github.malczuuu.natspring.connection.DefaultConnectionOptionsFactory;
import io.github.malczuuu.natspring.connection.JetStreamLifecycle;
import io.github.malczuuu.natspring.connection.ListenerContainerLifecycle;
import io.github.malczuuu.natspring.connection.ManagedConnectionLifecycle;
import io.github.malczuuu.natspring.connection.ManagedJetStreamLifecycle;
import io.github.malczuuu.natspring.connection.ManagedListenerContainerLifecycle;
import io.github.malczuuu.natspring.core.NatsMessageInterceptor;
import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.core.NatsPublishInterceptor;
import io.github.malczuuu.natspring.core.NatsTemplate;
import io.github.malczuuu.natspring.core.NatsTemplateBuilderCustomizer;
import io.github.malczuuu.natspring.handler.JetStreamListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natspring.handler.JetStreamListenerEndpointRegistry;
import io.github.malczuuu.natspring.handler.JetStreamMessageListenerContainer;
import io.github.malczuuu.natspring.handler.MessageArgumentResolver;
import io.github.malczuuu.natspring.handler.NatsListenerAnnotationBeanPostProcessor;
import io.github.malczuuu.natspring.handler.NatsListenerEndpointRegistry;
import io.github.malczuuu.natspring.handler.NatsMessageListenerContainer;
import io.github.malczuuu.natspring.handler.SimpleMessageArgumentResolver;
import io.github.malczuuu.natspring.instrument.JetStreamListenerObserver;
import io.github.malczuuu.natspring.instrument.NatsConnectionObserver;
import io.github.malczuuu.natspring.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.api.StreamConfiguration;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring Boot auto-configuration for core NATS infrastructure.
 *
 * @since 0.1.0
 */
@AutoConfiguration
@ConditionalOnBooleanProperty(name = "nats.enabled", matchIfMissing = true)
@ConditionalOnClass({Connection.class, JsonMapper.class})
@EnableConfigurationProperties(NatsProperties.class)
public final class NatsAutoConfiguration {

  /** Creates a new {@link NatsAutoConfiguration}. */
  public NatsAutoConfiguration() {}

  @Bean
  @ConditionalOnMissingBean(NatsConnectionDetails.class)
  PropertiesNatsConnectionDetails propertiesNatsConnectionDetails(NatsProperties properties) {
    return new PropertiesNatsConnectionDetails(properties);
  }

  @Bean
  @ConditionalOnMissingBean(ConnectionOptionsFactory.class)
  DefaultConnectionOptionsFactory natsConnectionOptionsFactory(
      Environment environment,
      NatsProperties properties,
      NatsConnectionDetails connectionDetails,
      NatsConnectionObserver connectionObserver,
      ObjectProvider<ConnectionOptionsBuilderCustomizer> customizers) {
    DefaultConnectionOptionsFactory connectionOptionsFactory =
        new DefaultConnectionOptionsFactory();
    connectionOptionsFactory.registerCustomizer(
        new PropertiesOptionsBuilderCustomizer(environment, properties, connectionDetails));
    connectionOptionsFactory.registerCustomizer(
        new WatcherOptionsBuilderCustomizer(connectionObserver));
    customizers.orderedStream().forEach(connectionOptionsFactory::registerCustomizer);
    return connectionOptionsFactory;
  }

  @Bean
  @ConditionalOnMissingBean(JetStreamLifecycle.class)
  ManagedJetStreamLifecycle managedJetStreamLifecycle(
      NatsProperties properties,
      ConnectionLifecycle connectionLifecycle,
      ObjectProvider<StreamConfiguration> streamConfigurations) {
    return new ManagedJetStreamLifecycle(
        connectionLifecycle,
        streamConfigurations.orderedStream().toList(),
        properties.isAutoStreamCreation());
  }

  @Bean
  @ConditionalOnMissingBean(ConnectionLifecycle.class)
  ManagedConnectionLifecycle managedConnectionLifecycle(
      ConnectionOptionsFactory connectionOptionsFactory) {
    return new ManagedConnectionLifecycle(connectionOptionsFactory.getOptions());
  }

  @Bean
  @ConditionalOnMissingBean(ListenerContainerLifecycle.class)
  ManagedListenerContainerLifecycle managedListenerContainerLifecycle(
      ManagedConnectionLifecycle managedConnection,
      NatsProperties properties,
      NatsListenerEndpointRegistry natsListenerEndpointRegistry,
      JetStreamListenerEndpointRegistry jetStreamListenerEndpointRegistry,
      NatsListenerObserver natsListenerObserver,
      JetStreamListenerObserver jetStreamListenerObserver,
      ObjectProvider<NatsMessageInterceptor> messageInterceptors,
      JsonMapper jsonMapper) {
    MessageArgumentResolver argumentResolver = new SimpleMessageArgumentResolver(jsonMapper);
    List<NatsMessageInterceptor> orderedInterceptors = messageInterceptors.orderedStream().toList();
    return new ManagedListenerContainerLifecycle(
        managedConnection,
        List.of(
            new NatsMessageListenerContainer(
                natsListenerEndpointRegistry,
                argumentResolver,
                natsListenerObserver,
                orderedInterceptors),
            new JetStreamMessageListenerContainer(
                jetStreamListenerEndpointRegistry,
                argumentResolver,
                jetStreamListenerObserver,
                properties.getPullFetchBatchSize(),
                properties.getPullFetchTimeout(),
                orderedInterceptors)));
  }

  @Bean
  @ConditionalOnMissingBean(NatsTemplate.Builder.class)
  NatsTemplate.Builder natsTemplateBuilder(
      Connection connection,
      JsonMapper jsonMapper,
      ObjectProvider<NatsPublishInterceptor> publishInterceptors,
      ObjectProvider<NatsTemplateBuilderCustomizer> customizers) {
    NatsTemplate.Builder builder =
        NatsTemplate.builder()
            .withConnection(connection)
            .withJsonMapper(jsonMapper)
            .addInterceptors(publishInterceptors.orderedStream().toList());
    for (NatsTemplateBuilderCustomizer customizer : customizers.orderedStream().toList()) {
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
