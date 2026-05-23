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

import io.github.malczuuu.natsify.annotation.AckMode;
import io.github.malczuuu.natsify.annotation.ConsumerType;
import io.github.malczuuu.natsify.annotation.DeliverPolicyType;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;

public final class JetStreamListenerHandle {

  private final Object bean;
  private final Method method;
  private final String subject;
  private final String stream;
  private final String durable;
  private final String queue;
  private final ConsumerType consumerType;
  private final AckMode ackMode;
  private final DeliverPolicyType deliverPolicy;

  private JetStreamListenerHandle(
      Object bean,
      Method method,
      String subject,
      String stream,
      String durable,
      String queue,
      ConsumerType consumerType,
      AckMode ackMode,
      DeliverPolicyType deliverPolicy) {
    this.bean = bean;
    this.method = method;
    this.subject = subject;
    this.stream = stream;
    this.durable = durable;
    this.queue = queue;
    this.consumerType = consumerType;
    this.ackMode = ackMode;
    this.deliverPolicy = deliverPolicy;
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

  public String getStream() {
    return stream;
  }

  public String getDurable() {
    return durable;
  }

  public String getQueue() {
    return queue;
  }

  public ConsumerType getConsumerType() {
    return consumerType;
  }

  public AckMode getAckMode() {
    return ackMode;
  }

  public DeliverPolicyType getDeliverPolicy() {
    return deliverPolicy;
  }

  @Override
  public String toString() {
    return ("[bean=" + AopUtils.getTargetClass(bean).getSimpleName())
        + (", method=" + method.getName())
        + (", subject=" + subject)
        + (", stream=" + stream)
        + (", durable=" + durable)
        + (", queue=" + queue)
        + (", consumerType=" + consumerType)
        + (", ackMode=" + ackMode)
        + (", deliverPolicy=" + deliverPolicy + "]");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable Object bean;
    private @Nullable Method method;
    private @Nullable String subject;
    private @Nullable String stream;
    private @Nullable String durable;
    private @Nullable String queue;
    private @Nullable ConsumerType consumerType;
    private @Nullable AckMode ackMode;
    private @Nullable DeliverPolicyType deliverPolicy;

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

    public Builder withStream(@Nullable String stream) {
      this.stream = stream;
      return this;
    }

    public Builder withDurable(@Nullable String durable) {
      this.durable = durable;
      return this;
    }

    public Builder withQueue(@Nullable String queue) {
      this.queue = queue;
      return this;
    }

    public Builder withConsumerType(@Nullable ConsumerType consumerType) {
      this.consumerType = consumerType;
      return this;
    }

    public Builder withAckMode(@Nullable AckMode ackMode) {
      this.ackMode = ackMode;
      return this;
    }

    public Builder withDeliverPolicy(@Nullable DeliverPolicyType deliverPolicy) {
      this.deliverPolicy = deliverPolicy;
      return this;
    }

    public JetStreamListenerHandle build() {
      if (bean == null) throw new IllegalStateException("bean is required");
      if (method == null) throw new IllegalStateException("method is required");
      if (subject == null) throw new IllegalStateException("subject is required");
      if (stream == null) throw new IllegalStateException("stream is required");
      if (durable == null) throw new IllegalStateException("durable is required");
      if (queue == null) throw new IllegalStateException("queue is required");
      if (consumerType == null) throw new IllegalStateException("consumerType is required");
      if (ackMode == null) throw new IllegalStateException("ackMode is required");
      if (deliverPolicy == null) throw new IllegalStateException("deliverPolicy is required");
      return new JetStreamListenerHandle(
          bean, method, subject, stream, durable, queue, consumerType, ackMode, deliverPolicy);
    }
  }
}
