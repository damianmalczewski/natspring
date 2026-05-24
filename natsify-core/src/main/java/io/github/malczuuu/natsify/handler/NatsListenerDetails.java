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

import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;

public final class NatsListenerDetails {

  private final Object bean;
  private final Method method;
  private final String subject;
  private final String queue;

  private NatsListenerDetails(Object bean, Method method, String subject, String queue) {
    this.bean = bean;
    this.method = method;
    this.subject = subject;
    this.queue = queue;
  }

  public Object getBean() {
    return bean;
  }

  public Method getMethod() {
    return method;
  }

  public String getSubject() {
    return subject;
  }

  public String getQueue() {
    return queue;
  }

  @Override
  public String toString() {
    return ("[bean=" + AopUtils.getTargetClass(bean).getSimpleName())
        + (", method=" + method.getName())
        + (", subject=" + subject)
        + (", queue=" + queue + "]");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable Object bean;
    private @Nullable Method method;
    private @Nullable String subject;
    private @Nullable String queue;

    private Builder() {}

    public Builder withBean(@Nullable Object bean) {
      this.bean = bean;
      return this;
    }

    public Builder withMethod(@Nullable Method method) {
      this.method = method;
      return this;
    }

    public Builder withSubject(@Nullable String subject) {
      this.subject = subject;
      return this;
    }

    public Builder withQueue(@Nullable String queue) {
      this.queue = queue;
      return this;
    }

    public NatsListenerDetails build() {
      if (bean == null) throw new IllegalStateException("bean is required");
      if (method == null) throw new IllegalStateException("method is required");
      if (subject == null) throw new IllegalStateException("subject is required");
      if (queue == null) throw new IllegalStateException("queue is required");
      return new NatsListenerDetails(bean, method, subject, queue);
    }
  }
}
