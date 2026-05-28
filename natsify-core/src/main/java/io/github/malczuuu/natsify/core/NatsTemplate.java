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

import io.github.malczuuu.natsify.connection.ConnectionSupplier;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

/**
 * Default {@link NatsOperations} implementation backed by a {@link
 * io.github.malczuuu.natsify.connection.ConnectionManager}.
 *
 * @since 0.1.0
 */
public class NatsTemplate implements NatsOperations {

  private final ConnectionSupplier connectionManager;
  private final JsonMapper jsonMapper;
  private final NatsPublishInterceptorChainExecution publishInterceptorChain;

  private NatsTemplate(
      ConnectionSupplier connectionSupplier,
      JsonMapper jsonMapper,
      List<NatsPublishInterceptor> interceptors) {
    this.connectionManager = connectionSupplier;
    this.jsonMapper = jsonMapper;
    this.publishInterceptorChain = new NatsPublishInterceptorChainExecution(interceptors);
  }

  /**
   * Returns a builder for {@link NatsTemplate}.
   *
   * @since 0.1.0
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Publishes a pre-built {@link Message} as-is.
   *
   * @param message the message to publish
   * @since 0.1.0
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
   * @since 0.1.0
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
   * @since 0.1.0
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
   * @since 0.1.0
   */
  @Override
  public <T> void publish(String subject, T bodyAsObject) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .data(jsonMapper.writeValueAsBytes(bodyAsObject))
            .build());
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
    doPublish(NatsMessage.builder().subject(subject).headers(headers).data(body).build());
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
   * @since 0.1.0
   */
  @Override
  public <T> void publish(String subject, Headers headers, T bodyAsObject) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .headers(headers)
            .data(jsonMapper.writeValueAsBytes(bodyAsObject))
            .build());
  }

  private void doPublish(Message message) {
    publishInterceptorChain.execute(message, m -> connectionManager.getConnection().publish(m));
  }

  /**
   * Builder for {@link NatsTemplate}.
   *
   * @since 0.1.0
   */
  public static final class Builder {

    private @Nullable ConnectionSupplier connectionSupplier;
    private @Nullable JsonMapper jsonMapper;
    private final List<NatsPublishInterceptor> interceptors = new ArrayList<>();

    private Builder() {}

    /**
     * Sets the connection supplier used to obtain the active NATS connection.
     *
     * @param connectionSupplier the connection supplier; must not be {@code null}
     * @return this builder
     * @since 0.1.0
     */
    public Builder withConnectionSupplier(ConnectionSupplier connectionSupplier) {
      this.connectionSupplier = connectionSupplier;
      return this;
    }

    /**
     * Sets the JSON mapper used for object serialization. If not set, defaults to {@code
     * JsonMapper.builder().findAndAddModules().build()}.
     *
     * @param jsonMapper the JSON mapper
     * @return this builder
     * @since 0.1.0
     */
    public Builder withJsonMapper(JsonMapper jsonMapper) {
      this.jsonMapper = jsonMapper;
      return this;
    }

    /**
     * Adds all given interceptors to the publish interceptor chain.
     *
     * @param interceptors interceptors to add
     * @return this builder
     * @since 0.1.0
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
     * @since 0.1.0
     */
    public Builder addInterceptor(NatsPublishInterceptor interceptor) {
      this.interceptors.add(interceptor);
      return this;
    }

    /**
     * Builds a {@link NatsTemplate} from the current state of this builder. Requires {@link
     * #withConnectionSupplier(ConnectionSupplier)} to have been set.
     *
     * @return a new {@link NatsTemplate}
     * @throws IllegalArgumentException if {@code connectionSupplier} has not been set
     * @since 0.1.0
     */
    public NatsTemplate build() {
      if (connectionSupplier == null) {
        throw new IllegalArgumentException("connectionSupplier is required");
      }
      JsonMapper jsonMapper =
          this.jsonMapper != null
              ? this.jsonMapper
              : JsonMapper.builder().findAndAddModules().build();
      return new NatsTemplate(connectionSupplier, jsonMapper, interceptors);
    }
  }
}
