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

package io.github.malczuuu.natspring.handler;

import static io.github.malczuuu.natspring.handler.ListenerMethodValidation.validateJetStreamListenerMethod;

import io.github.malczuuu.natspring.annotation.AckMode;
import io.github.malczuuu.natspring.annotation.ConsumerType;
import io.github.malczuuu.natspring.annotation.DeliverPolicyType;
import io.github.malczuuu.natspring.annotation.ResolutionFailureAction;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;

/**
 * Holds metadata for a {@link
 * io.github.malczuuu.natspring.annotation.JetStreamListener @JetStreamListener}-annotated method,
 * including the target bean, method, subject, stream, durable consumer name, queue group, consumer
 * type, ack mode, deliver policy, and dead-letter configuration.
 *
 * @since 0.1.0
 */
public final class JetStreamListenerEndpoint {

  private final Object bean;
  private final Method method;
  private final String subject;
  private final String stream;
  private final String durable;
  private final String queue;
  private final ConsumerType consumerType;
  private final AckMode ackMode;
  private final DeliverPolicyType deliverPolicy;
  private final String deadLetterSubject;
  private final int deadLetterDeliveries;
  private final ResolutionFailureAction resolveFailure;

  private JetStreamListenerEndpoint(
      Object bean,
      Method method,
      String subject,
      String stream,
      String durable,
      String queue,
      ConsumerType consumerType,
      AckMode ackMode,
      DeliverPolicyType deliverPolicy,
      String deadLetterSubject,
      int deadLetterDeliveries,
      ResolutionFailureAction resolveFailure) {
    this.bean = bean;
    this.method = method;
    this.subject = subject;
    this.stream = stream;
    this.durable = durable;
    this.queue = queue;
    this.consumerType = consumerType;
    this.ackMode = ackMode;
    this.deliverPolicy = deliverPolicy;
    this.deadLetterSubject = deadLetterSubject;
    this.deadLetterDeliveries = deadLetterDeliveries;
    this.resolveFailure = resolveFailure;
  }

  /**
   * Returns the Spring bean that declares the listener method.
   *
   * @return the listener bean
   */
  public Object getBean() {
    return bean;
  }

  /**
   * Returns the listener method.
   *
   * @return the listener method
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Returns the NATS subject filter for the consumer.
   *
   * @return the NATS subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the JetStream stream name.
   *
   * @return the stream name
   */
  public String getStream() {
    return stream;
  }

  /**
   * Returns the durable consumer name, or an empty string for an ephemeral consumer.
   *
   * @return the durable consumer name
   */
  public String getDurable() {
    return durable;
  }

  /**
   * Returns the queue group name for push consumers, or an empty string if none.
   *
   * @return the queue group name
   */
  public String getQueue() {
    return queue;
  }

  /**
   * Returns the consumer type (push or pull).
   *
   * @return the consumer type
   */
  public ConsumerType getConsumerType() {
    return consumerType;
  }

  /**
   * Returns the acknowledgement mode for received messages.
   *
   * @return the ack mode
   */
  public AckMode getAckMode() {
    return ackMode;
  }

  /**
   * Returns the deliver policy controlling which messages the consumer receives.
   *
   * @return the deliver policy
   */
  public DeliverPolicyType getDeliverPolicy() {
    return deliverPolicy;
  }

  /**
   * Returns the subject to publish messages to after exhausting delivery attempts, or an empty
   * string if the dead-letter queue is disabled.
   *
   * @return the dead-letter subject
   */
  public String getDeadLetterSubject() {
    return deadLetterSubject;
  }

  /**
   * Returns the maximum number of delivery attempts before dead-lettering. {@code -1} means
   * unlimited.
   *
   * @return the max delivery count
   * @since 0.1.1
   */
  public int getDeadLetterDeliveries() {
    return deadLetterDeliveries;
  }

  /**
   * Returns the action to take when argument resolution (deserialization) fails.
   *
   * @return the resolve failure action
   * @since 0.4.0
   */
  public ResolutionFailureAction getResolveFailure() {
    return resolveFailure;
  }

  /**
   * Returns a string representation of this listener endpoint.
   *
   * @return string representation
   */
  @Override
  public String toString() {
    return ("JetStreamListenerEndpoint[bean=" + AopUtils.getTargetClass(bean).getSimpleName())
        + (", method=" + method.getName())
        + (", subject=" + subject)
        + (", stream=" + stream)
        + (", durable=" + durable)
        + (", queue=" + queue)
        + (", consumerType=" + consumerType)
        + (", ackMode=" + ackMode)
        + (", deliverPolicy=" + deliverPolicy)
        + (", deadLetterSubject=" + deadLetterSubject)
        + (", deadLetterDeliveries=" + deadLetterDeliveries)
        + (", resolveFailure=" + resolveFailure + "]");
  }

  /**
   * Returns a new {@link Builder} for constructing a {@link JetStreamListenerEndpoint} instance.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link JetStreamListenerEndpoint}. */
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
    private String deadLetterSubject = "";
    private int deadLetterDeliveries = -1;
    private ResolutionFailureAction resolveFailure = ResolutionFailureAction.TERM;

    private Builder() {}

    /**
     * Sets the target bean.
     *
     * @param bean the Spring bean that declares the listener method
     * @return this builder
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
     */
    public Builder withMethod(@Nullable Method method) {
      this.method = method;
      return this;
    }

    /**
     * Sets the NATS subject filter for the consumer.
     *
     * @param subject the NATS subject
     * @return this builder
     */
    public Builder withSubject(@Nullable String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the JetStream stream name.
     *
     * @param stream the stream name
     * @return this builder
     */
    public Builder withStream(@Nullable String stream) {
      this.stream = stream;
      return this;
    }

    /**
     * Sets the durable consumer name.
     *
     * @param durable the durable consumer name, or an empty string for ephemeral
     * @return this builder
     */
    public Builder withDurable(@Nullable String durable) {
      this.durable = durable;
      return this;
    }

    /**
     * Sets the queue group name.
     *
     * @param queue the queue group name, or an empty string for none
     * @return this builder
     */
    public Builder withQueue(@Nullable String queue) {
      this.queue = queue;
      return this;
    }

    /**
     * Sets the consumer type.
     *
     * @param consumerType push or pull consumer type
     * @return this builder
     */
    public Builder withConsumerType(@Nullable ConsumerType consumerType) {
      this.consumerType = consumerType;
      return this;
    }

    /**
     * Sets the acknowledgement mode.
     *
     * @param ackMode the ack mode for received messages
     * @return this builder
     */
    public Builder withAckMode(@Nullable AckMode ackMode) {
      this.ackMode = ackMode;
      return this;
    }

    /**
     * Sets the deliver policy.
     *
     * @param deliverPolicy the deliver policy controlling which messages the consumer receives
     * @return this builder
     */
    public Builder withDeliverPolicy(@Nullable DeliverPolicyType deliverPolicy) {
      this.deliverPolicy = deliverPolicy;
      return this;
    }

    /**
     * Sets the dead-letter subject. Empty string disables the dead-letter queue.
     *
     * @param deadLetterSubject subject to publish failed messages to
     * @return this builder
     */
    public Builder withDeadLetterSubject(String deadLetterSubject) {
      this.deadLetterSubject = deadLetterSubject;
      return this;
    }

    /**
     * Sets the maximum number of delivery attempts before dead-lettering. {@code -1} means
     * unlimited.
     *
     * @param deadLetterDeliveries max delivery count
     * @return this builder
     * @since 0.1.1
     */
    public Builder withDeadLetterDeliveries(int deadLetterDeliveries) {
      this.deadLetterDeliveries = deadLetterDeliveries;
      return this;
    }

    /**
     * Sets the action to take when argument resolution (deserialization) fails.
     *
     * @param resolveFailure the resolve failure action
     * @return this builder
     * @since 0.4.0
     */
    public Builder withResolveFailure(ResolutionFailureAction resolveFailure) {
      this.resolveFailure = resolveFailure;
      return this;
    }

    /**
     * Builds the {@link JetStreamListenerEndpoint} instance.
     *
     * @return a new {@link JetStreamListenerEndpoint}
     * @throws IllegalArgumentException if configuration constraints are violated
     */
    public JetStreamListenerEndpoint build() {
      if (bean == null) {
        throw new IllegalArgumentException("bean is required");
      }
      if (method == null) {
        throw new IllegalArgumentException("method is required");
      }
      if (!StringUtils.hasLength(subject)) {
        throw new IllegalArgumentException("subject is required");
      }
      if (stream == null) {
        throw new IllegalArgumentException("stream is required");
      }
      if (durable == null) {
        throw new IllegalArgumentException("durable is required");
      }
      if (queue == null) {
        throw new IllegalArgumentException("queue is required");
      }
      if (consumerType == null) {
        throw new IllegalArgumentException("consumerType is required");
      }
      if (ackMode == null) {
        throw new IllegalArgumentException("ackMode is required");
      }
      if (deliverPolicy == null) {
        throw new IllegalArgumentException("deliverPolicy is required");
      }
      if (consumerType == ConsumerType.PULL && !queue.isEmpty()) {
        throw new IllegalArgumentException("queue group is not supported for pull consumers");
      }
      if (!deadLetterSubject.isEmpty() && deadLetterDeliveries <= 0) {
        throw new IllegalArgumentException(
            "deadLetterDeliveries must be positive when deadLetterSubject is set");
      }
      if (deadLetterDeliveries > 0 && deadLetterSubject.isEmpty()) {
        throw new IllegalArgumentException(
            "deadLetterSubject is required when deadLetterDeliveries is set");
      }
      if (!deadLetterSubject.isEmpty() && ackMode == AckMode.MANUAL) {
        throw new IllegalArgumentException(
            "deadLetterSubject is not supported with MANUAL ack mode");
      }
      validateJetStreamListenerMethod(method);
      return new JetStreamListenerEndpoint(
          bean,
          method,
          subject,
          stream,
          durable,
          queue,
          consumerType,
          ackMode,
          deliverPolicy,
          deadLetterSubject,
          deadLetterDeliveries,
          resolveFailure);
    }
  }
}
