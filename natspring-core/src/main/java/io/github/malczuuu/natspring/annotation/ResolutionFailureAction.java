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
 * Action to take when argument resolution (deserialization) fails before the handler is invoked.
 *
 * @since 0.4.0
 */
public enum ResolutionFailureAction {

  /**
   * Terminate the message ({@code term()}). No redelivery will occur. If a dead-letter subject is
   * configured, the message is forwarded there before termination. This is the default.
   */
  TERM,

  /**
   * Negatively acknowledge the message ({@code nak()}). NATS will redeliver it according to the
   * consumer's backoff/max-deliver settings. If a dead-letter subject is configured and {@link
   * JetStreamListener#deadLetterDeliveries()} is exhausted, the message is forwarded to the DLQ and
   * terminated instead of being nacked. Note that a malformed payload will always fail
   * deserialization, so this option is useful only when the consumer may eventually be updated to
   * handle the format.
   */
  NAK,

  /**
   * Acknowledge the message ({@code ack()}) and silently discard it. No redelivery, no
   * dead-lettering.
   */
  DISCARD
}
