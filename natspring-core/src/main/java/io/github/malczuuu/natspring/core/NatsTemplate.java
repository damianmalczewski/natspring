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

import io.github.malczuuu.natspring.converter.JacksonNatsMessageConverter;
import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

/**
 * Default {@link NatsOperations} implementation backed by a {@link Connection}.
 *
 * @since 0.1.0
 */
public class NatsTemplate implements NatsOperations {

  private final Connection connection;
  private final NatsMessageConverter converter;
  private final NatsPublishInterceptorChainExecution publishInterceptorChain;

  private NatsTemplate(
      Connection connection,
      NatsMessageConverter converter,
      List<NatsPublishInterceptor> interceptors) {
    this.connection = connection;
    this.converter = converter;
    this.publishInterceptorChain = new NatsPublishInterceptorChainExecution(interceptors);
  }

  /**
   * Returns a builder for {@link NatsTemplate}.
   *
   * @return a new {@link NatsTemplate.Builder}
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Publishes a pre-built {@link Message} as-is.
   *
   * @param message the message to publish
   */
  @Override
  public void publish(Message message) {
    doPublish(message);
  }

  /**
   * Publishes raw bytes to the given subject.
   *
   * @param subject the NATS subject
   * @param body the message body
   */
  @Override
  public void publish(String subject, byte[] body) {
    doPublish(NatsMessage.builder().subject(subject).data(body).build());
  }

  /**
   * Publishes a string to the given subject, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param bodyAsString the message body
   */
  @Override
  public void publish(String subject, String bodyAsString) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .data(bodyAsString.getBytes(StandardCharsets.UTF_8))
            .build());
  }

  /**
   * Publishes an object to the given subject, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   */
  @Override
  public <T> void publish(String subject, T bodyAsObject) {
    doPublish(NatsMessage.builder().subject(subject).data(converter.toBytes(bodyAsObject)).build());
  }

  /**
   * Publishes raw bytes to the given subject with custom headers.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param body the message body
   */
  @Override
  public void publish(String subject, Headers headers, byte[] body) {
    doPublish(NatsMessage.builder().subject(subject).headers(headers).data(body).build());
  }

  /**
   * Publishes a string to the given subject with custom headers, encoded as UTF-8.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsString the message body
   */
  @Override
  public void publish(String subject, Headers headers, String bodyAsString) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .headers(headers)
            .data(bodyAsString.getBytes(StandardCharsets.UTF_8))
            .build());
  }

  /**
   * Publishes an object to the given subject with custom headers, serialized to JSON.
   *
   * @param subject the NATS subject
   * @param headers the message headers
   * @param bodyAsObject the object to serialize and publish
   * @param <T> the object type
   */
  @Override
  public <T> void publish(String subject, Headers headers, T bodyAsObject) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .headers(headers)
            .data(converter.toBytes(bodyAsObject))
            .build());
  }

  private void doPublish(Message message) {
    publishInterceptorChain.execute(message, connection::publish);
  }

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
  @Override
  public CompletableFuture<NatsReply> request(String subject, byte[] payload, Duration timeout) {
    return doRequest(NatsMessage.builder().subject(subject).data(payload).build(), timeout);
  }

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
  @Override
  public CompletableFuture<NatsReply> request(String subject, String payload, Duration timeout) {
    return doRequest(
        NatsMessage.builder()
            .subject(subject)
            .data(payload.getBytes(StandardCharsets.UTF_8))
            .build(),
        timeout);
  }

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
  @Override
  public <T> CompletableFuture<NatsReply> request(String subject, T payload, Duration timeout) {
    return doRequest(
        NatsMessage.builder().subject(subject).data(converter.toBytes(payload)).build(), timeout);
  }

  private CompletableFuture<NatsReply> doRequest(Message message, Duration timeout) {
    AtomicReference<@Nullable CompletableFuture<Message>> ref = new AtomicReference<>();
    publishInterceptorChain.execute(
        message, m -> ref.set(connection.requestWithTimeout(m, timeout)));
    CompletableFuture<Message> result = ref.get();
    return result != null
        ? result.thenApply(m -> new SimpleNatsReply(m, converter))
        : CompletableFuture.failedFuture(
            new IllegalStateException("request suppressed by interceptor"));
  }

  /** Builder for {@link NatsTemplate}. */
  public static class Builder {

    private @Nullable Connection connection;
    private @Nullable NatsMessageConverter converter;
    private final List<NatsPublishInterceptor> interceptors = new ArrayList<>();

    private Builder() {}

    /**
     * Sets the NATS connection.
     *
     * @param connection the NATS connection; must not be {@code null}
     * @return this builder
     */
    public Builder withConnection(Connection connection) {
      this.connection = connection;
      return this;
    }

    /**
     * Sets the converter used for object serialization. If not set, defaults to {@code new
     * JacksonNatsConverter()}.
     *
     * @param converter the converter
     * @return this builder
     */
    public Builder withConverter(NatsMessageConverter converter) {
      this.converter = converter;
      return this;
    }

    /**
     * Adds all given interceptors to the publish interceptor chain.
     *
     * @param interceptors interceptors to add
     * @return this builder
     */
    public Builder addInterceptors(List<NatsPublishInterceptor> interceptors) {
      this.interceptors.addAll(interceptors);
      return this;
    }

    /**
     * Adds a single interceptor to the publish interceptor chain.
     *
     * @param interceptor the interceptor to add
     * @return this builder
     */
    public Builder addInterceptor(NatsPublishInterceptor interceptor) {
      this.interceptors.add(interceptor);
      return this;
    }

    /**
     * Builds a {@link NatsTemplate} from the current state of this builder. Requires {@link
     * #withConnection(Connection)} to have been set.
     *
     * @return a new {@link NatsTemplate}
     * @throws IllegalArgumentException if {@code connection} has not been set
     */
    public NatsTemplate build() {
      if (connection == null) {
        throw new IllegalArgumentException("connection is required");
      }
      NatsMessageConverter converter =
          this.converter != null ? this.converter : new JacksonNatsMessageConverter();
      return new NatsTemplate(connection, converter, interceptors);
    }
  }
}
