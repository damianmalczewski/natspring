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

package io.github.malczuuu.natsify.core;

import io.nats.client.Message;
import io.nats.client.impl.Headers;

/** Contract for publishing messages to NATS subjects. */
public interface NatsOperations {

  /**
   * Publishes a pre-built {@link Message} as-is.
   *
   * @param message the message to publish
   */
  void publish(Message message);

  /**
   * Publishes raw bytes to the given subject.
   *
   * @param subject the NATS subject
   * @param body the message body
   */
  void publish(String subject, byte[] body);

  /**
   * Publishes a string to the given subject, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param bodyAsString the message body
   */
  void publish(String subject, String bodyAsString);

  /**
   * Publishes an object to the given subject, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   */
  <T> void publish(String subject, T bodyAsObject);

  /**
   * Publishes raw bytes to the given subject with custom headers.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param body the message body
   */
  void publish(String subject, Headers headers, byte[] body);

  /**
   * Publishes a string to the given subject with custom headers, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsString the message body
   */
  void publish(String subject, Headers headers, String bodyAsString);

  /**
   * Publishes an object to the given subject with custom headers, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   */
  <T> void publish(String subject, Headers headers, T bodyAsObject);
}
