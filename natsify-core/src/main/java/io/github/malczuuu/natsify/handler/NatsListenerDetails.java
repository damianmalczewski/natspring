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

import static io.github.malczuuu.natsify.handler.ListenerMethodValidation.validateNatsListenerMethod;

import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;

/**
 * Holds metadata for a {@link
 * io.github.malczuuu.natsify.annotation.NatsListener @NatsListener}-annotated method, including the
 * target bean, method, subject, and queue group.
 *
 * @since 0.1.0
 */
public final class NatsListenerDetails {

  private final Object bean;
  private final Method method;
  private final String subject;
  private final String queue;
  private final String deadLetterSubject;

  private NatsListenerDetails(
      Object bean, Method method, String subject, String queue, String deadLetterSubject) {
    this.bean = bean;
    this.method = method;
    this.subject = subject;
    this.queue = queue;
    this.deadLetterSubject = deadLetterSubject;
  }

  /**
   * Returns the Spring bean that declares the listener method.
   *
   * @return the listener bean
   * @since 0.1.0
   */
  public Object getBean() {
    return bean;
  }

  /**
   * Returns the listener method.
   *
   * @return the listener method
   * @since 0.1.0
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Returns the NATS subject the listener subscribes to.
   *
   * @return the NATS subject
   * @since 0.1.0
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the queue group name, or an empty string if none was specified.
   *
   * @return the queue group name
   * @since 0.1.0
   */
  public String getQueue() {
    return queue;
  }

  /**
   * Returns the dead-letter subject, or an empty string if dead-lettering is disabled.
   *
   * @return the dead-letter subject
   * @since 0.1.0
   */
  public String getDeadLetterSubject() {
    return deadLetterSubject;
  }

  /**
   * Returns a string representation of this listener details.
   *
   * @return string representation
   * @since 0.1.0
   */
  @Override
  public String toString() {
    return ("NatsListenerDetails[bean=" + AopUtils.getTargetClass(bean).getSimpleName())
        + (", method=" + method.getName())
        + (", subject=" + subject)
        + (", queue=" + queue + "]");
  }

  /**
   * Returns a new {@link Builder} for constructing a {@link NatsListenerDetails} instance.
   *
   * @return a new builder
   * @since 0.1.0
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for {@link NatsListenerDetails}.
   *
   * @since 0.1.0
   */
  public static final class Builder {

    private @Nullable Object bean;
    private @Nullable Method method;
    private @Nullable String subject;
    private @Nullable String queue;
    private String deadLetterSubject = "";

    private Builder() {}

    /**
     * Sets the target bean.
     *
     * @param bean the Spring bean that declares the listener method
     * @return this builder
     * @since 0.1.0
     */
    public Builder withBean(@Nullable Object bean) {
      this.bean = bean;
      return this;
    }

    /**
     * Sets the listener method.
     *
     * @param method the annotated method
     * @return this builder
     * @since 0.1.0
     */
    public Builder withMethod(@Nullable Method method) {
      this.method = method;
      return this;
    }

    /**
     * Sets the NATS subject.
     *
     * @param subject the NATS subject to subscribe to
     * @return this builder
     * @since 0.1.0
     */
    public Builder withSubject(@Nullable String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the queue group name.
     *
     * @param queue the queue group name, or an empty string for none
     * @return this builder
     * @since 0.1.0
     */
    public Builder withQueue(@Nullable String queue) {
      this.queue = queue;
      return this;
    }

    /**
     * Sets the dead-letter subject.
     *
     * @param deadLetterSubject the subject to publish failed messages to, or an empty string to
     *     disable dead-lettering
     * @return this builder
     * @since 0.1.0
     */
    public Builder withDeadLetterSubject(String deadLetterSubject) {
      this.deadLetterSubject = deadLetterSubject;
      return this;
    }

    /**
     * Builds the {@link NatsListenerDetails} instance.
     *
     * @return a new {@link NatsListenerDetails}
     * @throws IllegalArgumentException if configuration constraints are violated
     * @since 0.1.0
     */
    public NatsListenerDetails build() {
      if (bean == null) {
        throw new IllegalArgumentException("bean is required");
      }
      if (method == null) {
        throw new IllegalArgumentException("method is required");
      }
      if (!StringUtils.hasLength(subject)) {
        throw new IllegalArgumentException("subject is required");
      }
      if (queue == null) {
        throw new IllegalArgumentException("queue is required");
      }
      validateNatsListenerMethod(method);
      return new NatsListenerDetails(bean, method, subject, queue, deadLetterSubject);
    }
  }
}
