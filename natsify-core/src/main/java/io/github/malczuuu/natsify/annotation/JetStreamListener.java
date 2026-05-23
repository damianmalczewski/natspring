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
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JetStreamListener {

  /**
   * NATS subject to subscribe to. Supports property placeholders (e.g., {@code ${my.subject}}).
   *
   * @return the subject
   */
  String subject() default "";

  /**
   * Stream name to bind the consumer to. Optional if the subject uniquely identifies a stream.
   *
   * @return the stream name
   */
  String stream() default "";

  /**
   * Durable consumer name. Enables resuming from the last acknowledged position after a restart.
   *
   * @return the durable consumer name
   */
  String durable() default "";

  /**
   * Queue group name for load-balanced delivery across multiple instances.
   *
   * @return the queue group name
   */
  String queue() default "";

  /**
   * Push or pull delivery model for this consumer.
   *
   * @return the consumer type
   */
  ConsumerType consumerType() default ConsumerType.PULL;

  /**
   * How acknowledgment is handled after the method returns.
   *
   * @return the ack mode
   */
  AckMode ackMode() default AckMode.AUTO;

  /**
   * Which messages to receive when the consumer is first created.
   *
   * @return the deliver policy
   */
  DeliverPolicyType deliverPolicy() default DeliverPolicyType.NEW;
}
