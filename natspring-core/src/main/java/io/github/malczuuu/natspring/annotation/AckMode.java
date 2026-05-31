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

/**
 * Acknowledgment mode for JetStream message handlers.
 *
 * <p>Regardless of the chosen mode, the framework always terminates ({@code term()}) a message when
 * argument resolution fails before the handler is invoked.
 *
 * @since 0.1.0
 */
public enum AckMode {

  /**
   * Framework acknowledges the message on successful handler return. On handler exception, it naks
   * the message to allow redelivery; once the dead-letter delivery threshold is reached the message
   * is terminated ({@code term()}) and forwarded to the dead-letter subject (if configured).
   */
  AUTO,

  /**
   * Handler is responsible for calling {@code msg.ack()}, {@code msg.nak()}, or {@code msg.term()}
   * itself; the framework does nothing after the method returns.
   */
  MANUAL
}
