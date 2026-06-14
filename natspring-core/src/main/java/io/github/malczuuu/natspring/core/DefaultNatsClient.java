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
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

final class DefaultNatsClient implements NatsClient {

  final Connection connection;
  final NatsMessageConverter converter;
  final List<NatsPublishInterceptor> interceptors;
  private final NatsPublishInterceptorChainExecution publishInterceptorChain;

  DefaultNatsClient(
      Connection connection,
      NatsMessageConverter converter,
      List<NatsPublishInterceptor> interceptors) {
    this.connection = connection;
    this.converter = converter;
    this.interceptors = List.copyOf(interceptors);
    this.publishInterceptorChain = new NatsPublishInterceptorChainExecution(interceptors);
  }

  @Override
  public void publish(Message message) {
    doPublish(message);
  }

  @Override
  public void publish(String subject, byte[] body) {
    doPublish(NatsMessage.builder().subject(subject).data(body).build());
  }

  @Override
  public void publish(String subject, String bodyAsString) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .data(bodyAsString.getBytes(StandardCharsets.UTF_8))
            .build());
  }

  @Override
  public <T> void publish(String subject, T bodyAsObject) {
    doPublish(NatsMessage.builder().subject(subject).data(converter.toBytes(bodyAsObject)).build());
  }

  @Override
  public void publish(String subject, Headers headers, byte[] body) {
    doPublish(NatsMessage.builder().subject(subject).headers(headers).data(body).build());
  }

  @Override
  public void publish(String subject, Headers headers, String bodyAsString) {
    doPublish(
        NatsMessage.builder()
            .subject(subject)
            .headers(headers)
            .data(bodyAsString.getBytes(StandardCharsets.UTF_8))
            .build());
  }

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

  @Override
  public CompletableFuture<NatsReply> request(String subject, byte[] payload, Duration timeout) {
    return doRequest(NatsMessage.builder().subject(subject).data(payload).build(), timeout);
  }

  @Override
  public CompletableFuture<NatsReply> request(String subject, String payload, Duration timeout) {
    return doRequest(
        NatsMessage.builder()
            .subject(subject)
            .data(payload.getBytes(StandardCharsets.UTF_8))
            .build(),
        timeout);
  }

  @Override
  public <T> CompletableFuture<NatsReply> request(String subject, T payload, Duration timeout) {
    return doRequest(
        NatsMessage.builder().subject(subject).data(converter.toBytes(payload)).build(), timeout);
  }

  @Override
  public NatsClient.Builder mutate() {
    return new DefaultBuilder(this);
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

  static final class DefaultBuilder implements NatsClient.Builder {

    private @Nullable Connection connection;
    private @Nullable NatsMessageConverter converter;
    private final List<NatsPublishInterceptor> interceptors = new ArrayList<>();

    DefaultBuilder() {}

    DefaultBuilder(DefaultNatsClient client) {
      this.connection = client.connection;
      this.converter = client.converter;
      this.interceptors.addAll(client.interceptors);
    }

    @Override
    public NatsClient.Builder withConnection(Connection connection) {
      this.connection = connection;
      return this;
    }

    @Override
    public NatsClient.Builder withConverter(NatsMessageConverter converter) {
      this.converter = converter;
      return this;
    }

    @Override
    public NatsClient.Builder addInterceptors(List<NatsPublishInterceptor> interceptors) {
      this.interceptors.addAll(interceptors);
      return this;
    }

    @Override
    public NatsClient.Builder addInterceptor(NatsPublishInterceptor interceptor) {
      this.interceptors.add(interceptor);
      return this;
    }

    @Override
    public NatsClient build() {
      if (connection == null) {
        throw new IllegalArgumentException("connection is required");
      }
      if (converter == null) {
        throw new IllegalArgumentException("converter is required");
      }
      return new DefaultNatsClient(connection, converter, interceptors);
    }
  }
}
