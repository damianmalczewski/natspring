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

package io.github.malczuuu.natspring.core;

import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Contract for publishing messages to and sending requests over NATS subjects.
 *
 * <p>Use {@link #builder()} to create an instance, or {@link #mutate()} on an existing instance to
 * create a copy-builder pre-populated with its state.
 *
 * @since 0.1.0
 */
public interface NatsOperations {

  /**
   * Returns a new builder for constructing a {@link NatsOperations} instance.
   *
   * @return a new builder
   */
  static Builder builder() {
    return new NatsTemplate.DefaultBuilder();
  }

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

  /**
   * Sends a request to the given subject and returns a future that completes with the reply.
   * Completes exceptionally with {@link java.util.concurrent.TimeoutException} if no reply arrives
   * within the given timeout, or with {@link IllegalStateException} if the request was suppressed
   * by a {@link NatsPublishInterceptor}.
   *
   * @param subject the NATS subject
   * @param payload the request body
   * @param timeout how long to wait for a reply
   * @return future that completes with the reply message
   */
  CompletableFuture<NatsReply> request(String subject, byte[] payload, Duration timeout);

  /**
   * Sends a request to the given subject and returns a future that completes with the reply.
   * Completes exceptionally with {@link java.util.concurrent.TimeoutException} if no reply arrives
   * within the given timeout, or with {@link IllegalStateException} if the request was suppressed
   * by a {@link NatsPublishInterceptor}.
   *
   * @param subject the NATS subject
   * @param payload the request body, encoded as UTF-8
   * @param timeout how long to wait for a reply
   * @return future that completes with the reply message
   */
  CompletableFuture<NatsReply> request(String subject, String payload, Duration timeout);

  /**
   * Sends a request to the given subject and returns a future that completes with the reply.
   * Completes exceptionally with {@link java.util.concurrent.TimeoutException} if no reply arrives
   * within the given timeout, or with {@link IllegalStateException} if the request was suppressed
   * by a {@link NatsPublishInterceptor}.
   *
   * @param subject the NATS subject
   * @param payload the request body, serialized to JSON
   * @param timeout how long to wait for a reply
   * @param <T> the payload object type
   * @return future that completes with the reply message
   */
  <T> CompletableFuture<NatsReply> request(String subject, T payload, Duration timeout);

  /**
   * Returns a builder pre-populated with the state of this {@link NatsOperations}, allowing
   * modifications without changing the original.
   *
   * @return a new builder pre-populated with this instance's state
   */
  Builder mutate();

  /**
   * Builder for {@link NatsOperations}.
   *
   * @since 0.4.0
   */
  interface Builder {

    /**
     * Sets the NATS connection.
     *
     * @param connection the NATS connection; must not be {@code null}
     * @return this builder
     */
    Builder withConnection(Connection connection);

    /**
     * Sets the converter used for object serialization.
     *
     * @param converter the converter
     * @return this builder
     */
    Builder withConverter(NatsMessageConverter converter);

    /**
     * Adds all given interceptors to the publish interceptor chain.
     *
     * @param interceptors interceptors to add
     * @return this builder
     */
    Builder addInterceptors(List<NatsPublishInterceptor> interceptors);

    /**
     * Adds a single interceptor to the publish interceptor chain.
     *
     * @param interceptor the interceptor to add
     * @return this builder
     */
    Builder addInterceptor(NatsPublishInterceptor interceptor);

    /**
     * Builds a {@link NatsOperations} from the current state of this builder. Validates if required
     * values have been set.
     *
     * @return a new {@link NatsOperations}
     * @throws IllegalArgumentException if any required field has not been set
     */
    NatsOperations build();
  }
}
