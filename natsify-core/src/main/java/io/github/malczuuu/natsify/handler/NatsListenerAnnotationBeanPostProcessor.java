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

import io.github.malczuuu.natsify.annotation.NatsListener;
import java.lang.reflect.Method;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringValueResolver;

/**
 * Scans Spring beans for {@link NatsListener @NatsListener}-annotated methods and registers them
 * with {@link NatsListenerRegistry}.
 *
 * @since 0.1.0
 */
public class NatsListenerAnnotationBeanPostProcessor
    implements BeanPostProcessor, EmbeddedValueResolverAware {

  private static final Logger log =
      LoggerFactory.getLogger(NatsListenerAnnotationBeanPostProcessor.class);

  private final NatsListenerRegistry registry;

  private StringValueResolver valueResolver = value -> value;

  /**
   * Creates a new {@code NatsListenerAnnotationBeanPostProcessor}.
   *
   * @param registry registry to register discovered listeners with
   * @since 0.1.0
   */
  public NatsListenerAnnotationBeanPostProcessor(NatsListenerRegistry registry) {
    this.registry = registry;
  }

  /**
   * Sets the resolver used to evaluate placeholder expressions in annotation attributes.
   *
   * @param resolver the embedded value resolver
   * @since 0.1.0
   */
  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.valueResolver = resolver;
  }

  /**
   * Scans the bean for {@link NatsListener @NatsListener} methods and registers each as a {@link
   * NatsListenerDetails}.
   *
   * @param bean the bean instance
   * @param beanName the bean name
   * @return the bean instance unchanged
   * @throws BeansException if post-processing fails
   * @since 0.1.0
   */
  @Override
  public @Nullable Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    Class<?> clazz = AopUtils.getTargetClass(bean);

    Map<Method, NatsListener> methods = filterMethods(clazz);

    if (methods.isEmpty()) {
      return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    for (Map.Entry<Method, NatsListener> entry : methods.entrySet()) {
      Method method = entry.getKey();
      NatsListener annotation = entry.getValue();
      String subject = resolve(annotation.subject());
      String queue = resolve(annotation.queue());
      String deadLetterSubject = resolve(annotation.deadLetterSubject());

      NatsListenerDetails listener =
          NatsListenerDetails.builder()
              .withBean(bean)
              .withMethod(method)
              .withSubject(subject)
              .withQueue(queue)
              .withDeadLetterSubject(deadLetterSubject)
              .build();

      registry.register(listener);
      log.info("Registered @NatsListener to {}", listener);
    }

    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }

  private String resolve(String value) {
    String resolved = valueResolver.resolveStringValue(value);
    return resolved != null ? resolved : "";
  }

  private Map<Method, NatsListener> filterMethods(Class<?> clazz) {
    return MethodIntrospector.selectMethods(
        clazz,
        (MethodIntrospector.MetadataLookup<NatsListener>)
            method -> AnnotationUtils.findAnnotation(method, NatsListener.class));
  }
}
