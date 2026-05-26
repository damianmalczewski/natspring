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

import io.github.malczuuu.natsify.connection.ConnectionManager;
import io.github.malczuuu.natsify.connection.ConnectionSupplier;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.nio.charset.StandardCharsets;
import tools.jackson.databind.json.JsonMapper;

/**
 * Default {@link NatsOperations} implementation backed by a {@link ConnectionManager}.
 *
 * @since 0.1.0
 */
public class NatsTemplate implements NatsOperations {

  private final ConnectionSupplier connectionManager;
  private final JsonMapper jsonMapper;

  /**
   * Creates a new {@link NatsTemplate} with the given connection and JSON mapper.
   *
   * @param connectionSupplier provides the active NATS connection
   * @param jsonMapper used for JSON serialization in {@link #publish(String, Object)}
   * @since 0.1.0
   */
  public NatsTemplate(ConnectionSupplier connectionSupplier, JsonMapper jsonMapper) {
    this.connectionManager = connectionSupplier;
    this.jsonMapper = jsonMapper;
  }

  /**
   * Publishes a pre-built {@link Message} as-is.
   *
   * @param message the message to publish
   * @since 0.1.0
   */
  @Override
  public void publish(Message message) {
    connectionManager.getConnection().publish(message);
  }

  /**
   * Publishes raw bytes to the given subject.
   *
   * @param subject the NATS subject
   * @param body the message body
   * @since 0.1.0
   */
  @Override
  public void publish(String subject, byte[] body) {
    connectionManager.getConnection().publish(subject, body);
  }

  /**
   * Publishes a string to the given subject, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param bodyAsString the message body
   * @since 0.1.0
   */
  @Override
  public void publish(String subject, String bodyAsString) {
    connectionManager
        .getConnection()
        .publish(subject, bodyAsString.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Publishes an object to the given subject, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   * @since 0.1.0
   */
  @Override
  public <T> void publish(String subject, T bodyAsObject) {
    connectionManager.getConnection().publish(subject, jsonMapper.writeValueAsBytes(bodyAsObject));
  }

  /**
   * Publishes raw bytes to the given subject with custom headers.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param body the message body
   * @since 0.1.0
   */
  @Override
  public void publish(String subject, Headers headers, byte[] body) {
    connectionManager.getConnection().publish(subject, headers, body);
  }

  /**
   * Publishes a string to the given subject with custom headers, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsString the message body
   * @since 0.1.0
   */
  @Override
  public void publish(String subject, Headers headers, String bodyAsString) {
    connectionManager
        .getConnection()
        .publish(subject, headers, bodyAsString.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Publishes an object to the given subject with custom headers, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   * @since 0.1.0
   */
  @Override
  public <T> void publish(String subject, Headers headers, T bodyAsObject) {
    connectionManager
        .getConnection()
        .publish(subject, headers, jsonMapper.writeValueAsBytes(bodyAsObject));
  }
}
