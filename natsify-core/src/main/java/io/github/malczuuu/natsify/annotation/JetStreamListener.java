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

package io.github.malczuuu.natsify.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a JetStream message listener. The method is registered with the JetStream
 * consumer infrastructure on application startup.
 *
 * @since 0.1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JetStreamListener {

  /**
   * NATS subject to subscribe to. Supports property placeholders (e.g., {@code ${my.subject}}).
   *
   * @return the subject
   * @since 0.1.0
   */
  String subject() default "";

  /**
   * Stream name to bind the consumer to. Optional if the subject uniquely identifies a stream.
   * Supports property placeholders (e.g., {@code ${my.stream}}).
   *
   * @return the stream name
   * @since 0.1.0
   */
  String stream() default "";

  /**
   * Durable consumer name. Enables resuming from the last acknowledged position after a restart.
   * Supports property placeholders (e.g., {@code ${my.durable}}).
   *
   * @return the durable consumer name
   * @since 0.1.0
   */
  String durable() default "";

  /**
   * Queue group name for load-balanced delivery across multiple instances. Supports property
   * placeholders (e.g., {@code ${my.queue}}).
   *
   * @return the queue group name
   * @since 0.1.0
   */
  String queue() default "";

  /**
   * Push or pull delivery model for this consumer.
   *
   * @return the consumer type
   * @since 0.1.0
   */
  ConsumerType consumerType() default ConsumerType.PULL;

  /**
   * How acknowledgment is handled after the method returns.
   *
   * @return the ack mode
   * @since 0.1.0
   */
  AckMode ackMode() default AckMode.AUTO;

  /**
   * Which messages to receive when the consumer is first created.
   *
   * @return the deliver policy
   * @since 0.1.0
   */
  DeliverPolicyType deliverPolicy() default DeliverPolicyType.NEW;

  /**
   * Subject to publish failed messages to. Empty string disables dead-lettering. Supports property
   * placeholders (e.g., {@code ${my.dlq}}).
   *
   * <p>Requires {@link #maxDeliveries()} to be positive. Not compatible with {@link
   * AckMode#MANUAL}.
   *
   * <p>Delivery semantics differ by failure type:
   *
   * <ul>
   *   <li><b>Deserialization failure</b> - message is terminated and dead-lettered immediately on
   *       the first delivery attempt. Retrying a malformed payload would never succeed.
   *   <li><b>Handler invocation failure</b> - message is nacked and redelivered until {@link
   *       #maxDeliveries()} is exhausted, then terminated and dead-lettered.
   * </ul>
   *
   * @return the dead-letter subject
   * @since 0.1.0
   */
  String deadLetterSubject() default "";

  /**
   * Maximum number of delivery attempts before the message is dead-lettered. {@code -1} means
   * unlimited. When positive, sets the JetStream consumer {@code maxDeliver} configuration.
   *
   * @return the max delivery count
   * @since 0.1.0
   */
  int maxDeliveries() default -1;
}
