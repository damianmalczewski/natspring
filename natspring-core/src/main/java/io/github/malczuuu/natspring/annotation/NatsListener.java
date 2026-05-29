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

package io.github.malczuuu.natspring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a NATS core (non-JetStream) message listener. Messages are delivered with no
 * persistence and no acknowledgment.
 *
 * @since 0.1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NatsListener {

  /**
   * NATS subject to subscribe to. Supports property placeholders (e.g., {@code ${my.subject}}).
   *
   * @return the subject
   * @since 0.1.0
   */
  String subject() default "";

  /**
   * Queue group name for load-balanced delivery across multiple instances. Supports property
   * placeholders (e.g., {@code ${my.queue}}).
   *
   * @return the queue group name
   * @since 0.1.0
   */
  String queue() default "";

  /**
   * Subject to publish failed messages to. Empty string disables dead-lettering. Supports property
   * placeholders (e.g., {@code ${my.dlq}}).
   *
   * <p>Core NATS provides no persistence or redelivery, so dead-lettering is at-most-once:
   *
   * <ul>
   *   <li><b>Argument resolution failure</b> - message is dead-lettered immediately; retrying a
   *       malformed payload would never succeed.
   *   <li><b>Handler invocation failure</b> - message is dead-lettered immediately on first
   *       failure; there is no retry mechanism in core NATS.
   * </ul>
   *
   * <p>The publish itself is best-effort: if it fails, the failure is logged and the original
   * message is silently dropped.
   *
   * @return the dead-letter subject
   * @since 0.1.0
   */
  String deadLetterSubject() default "";
}
