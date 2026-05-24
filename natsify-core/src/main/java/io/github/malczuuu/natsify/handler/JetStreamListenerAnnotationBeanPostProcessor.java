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

package io.github.malczuuu.natsify.handler;

import io.github.malczuuu.natsify.annotation.JetStreamListener;
import java.lang.reflect.Method;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringValueResolver;

public class JetStreamListenerAnnotationBeanPostProcessor
    implements BeanPostProcessor, EmbeddedValueResolverAware {

  private static final Logger log =
      LoggerFactory.getLogger(JetStreamListenerAnnotationBeanPostProcessor.class);

  private final JetStreamListenerRegistry jetStreamListenerRegistry;

  private StringValueResolver valueResolver = value -> value;

  public JetStreamListenerAnnotationBeanPostProcessor(
      JetStreamListenerRegistry jetStreamListenerRegistry) {
    this.jetStreamListenerRegistry = jetStreamListenerRegistry;
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.valueResolver = resolver;
  }

  @Override
  public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) {
    Class<?> clazz = AopUtils.getTargetClass(bean);

    Map<Method, JetStreamListener> methods = filterMethods(clazz);

    if (methods.isEmpty()) {
      return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    for (Map.Entry<Method, JetStreamListener> entry : methods.entrySet()) {
      Method method = entry.getKey();
      JetStreamListener annotation = entry.getValue();
      String subject = resolve(annotation.subject());
      String stream = resolve(annotation.stream());
      String durable = resolve(annotation.durable());
      String queue = resolve(annotation.queue());

      JetStreamListenerDetails listener =
          JetStreamListenerDetails.builder()
              .withBean(bean)
              .withMethod(method)
              .withSubject(subject)
              .withStream(stream)
              .withDurable(durable)
              .withQueue(queue)
              .withConsumerType(annotation.consumerType())
              .withAckMode(annotation.ackMode())
              .withDeliverPolicy(annotation.deliverPolicy())
              .build();

      jetStreamListenerRegistry.register(listener);
      log.info("Registered @JetStreamListener to {}", listener);
    }

    return bean;
  }

  private String resolve(String value) {
    String resolved = valueResolver.resolveStringValue(value);
    return resolved != null ? resolved : "";
  }

  private Map<Method, JetStreamListener> filterMethods(Class<?> clazz) {
    return MethodIntrospector.selectMethods(
        clazz,
        (MethodIntrospector.MetadataLookup<JetStreamListener>)
            method -> AnnotationUtils.findAnnotation(method, JetStreamListener.class));
  }
}
