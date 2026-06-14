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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class NatsClientTests {

  private Connection connection;
  private NatsMessageConverter converter;

  @BeforeEach
  void beforeEach() {
    connection = Mockito.mock(Connection.class);
    converter = Mockito.mock(NatsMessageConverter.class);
  }

  @Test
  void givenMissingConnection_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> NatsClient.builder().withConverter(converter).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("connection is required");
  }

  @Test
  void givenNoConverter_whenBuild_thenSucceeds() {
    assertThatThrownBy(() -> NatsClient.builder().withConnection(connection).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("converter is required");
  }

  @Test
  void givenNoInterceptors_whenPublishMessage_thenConnectionReceivesMessage() {
    Message message = NatsMessage.builder().subject("test").build();
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish(message);

    verify(connection).publish(message);
  }

  @Test
  void givenNoInterceptors_whenPublishBytes_thenConnectionReceivesCorrectSubjectAndBody() {
    byte[] body = {1, 2, 3};
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", body);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getSubject()).isEqualTo("test.subject");
    assertThat(captor.getValue().getData()).isEqualTo(body);
  }

  @Test
  void givenNoInterceptors_whenPublishString_thenConnectionReceivesUtf8Bytes() {
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", "hello");

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getData()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenNoInterceptors_whenPublishObject_thenConnectionReceivesJsonBytes() {
    byte[] json = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
    when(converter.toBytes(any())).thenReturn(json);
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", new Object());

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getData()).isEqualTo(json);
  }

  @Test
  void givenNoInterceptors_whenPublishBytesWithHeaders_thenConnectionReceivesHeaders() {
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", headers, new byte[0]);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getHeaders().getFirst("X-Foo")).isEqualTo("bar");
  }

  @Test
  void givenNoInterceptors_whenPublishStringWithHeaders_thenConnectionReceivesHeadersAndUtf8Body() {
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", headers, "hello");

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getHeaders().getFirst("X-Foo")).isEqualTo("bar");
    assertThat(captor.getValue().getData()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenNoInterceptors_whenPublishObjectWithHeaders_thenConnectionReceivesHeadersAndJsonBody() {
    byte[] json = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
    when(converter.toBytes(any())).thenReturn(json);
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.publish("test.subject", headers, new Object());

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getHeaders().getFirst("X-Foo")).isEqualTo("bar");
    assertThat(captor.getValue().getData()).isEqualTo(json);
  }

  @Test
  void givenInterceptor_whenPublish_thenInterceptorCalledBeforeConnection() {
    List<String> calls = new ArrayList<>();
    NatsPublishInterceptor interceptor =
        (msg, chain) -> {
          calls.add("interceptor");
          chain.proceed(msg);
        };
    NatsClient client =
        NatsClient.builder()
            .withConnection(connection)
            .withConverter(converter)
            .addInterceptor(interceptor)
            .build();

    client.publish("test.subject", new byte[0]);

    calls.add("verified");
    verify(connection).publish(any(Message.class));
    assertThat(calls).containsExactly("interceptor", "verified");
  }

  @Test
  void givenTwoInterceptors_whenPublish_thenCalledInOrder() {
    List<String> calls = new ArrayList<>();
    NatsPublishInterceptor first =
        (message, chain) -> {
          calls.add("first");
          chain.proceed(message);
        };
    NatsPublishInterceptor second =
        (message, chain) -> {
          calls.add("second");
          chain.proceed(message);
        };
    NatsClient client =
        NatsClient.builder()
            .withConnection(connection)
            .withConverter(converter)
            .addInterceptors(List.of(first, second))
            .build();

    client.publish("test.subject", new byte[0]);

    assertThat(calls).containsExactly("first", "second");
  }

  @Test
  void givenInterceptorThatReplacesMessage_whenPublish_thenConnectionReceivesReplacedMessage() {
    Message replacement = NatsMessage.builder().subject("replaced.subject").build();
    NatsPublishInterceptor interceptor = (msg, chain) -> chain.proceed(replacement);
    NatsClient client =
        NatsClient.builder()
            .withConnection(connection)
            .withConverter(converter)
            .addInterceptor(interceptor)
            .build();

    client.publish("original.subject", new byte[0]);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getSubject()).isEqualTo("replaced.subject");
  }

  @Test
  void givenNoInterceptors_whenRequestBytes_thenConnectionReceivesCorrectSubjectAndPayload() {
    byte[] payload = {1, 2, 3};
    when(connection.requestWithTimeout(any(Message.class), any(Duration.class)))
        .thenReturn(new CompletableFuture<>());
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.request("test.subject", payload, Duration.ofSeconds(1));

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).requestWithTimeout(captor.capture(), any(Duration.class));
    assertThat(captor.getValue().getSubject()).isEqualTo("test.subject");
    assertThat(captor.getValue().getData()).isEqualTo(payload);
  }

  @Test
  void givenNoInterceptors_whenRequestString_thenConnectionReceivesUtf8Payload() {
    when(connection.requestWithTimeout(any(Message.class), any(Duration.class)))
        .thenReturn(new CompletableFuture<>());
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.request("test.subject", "hello", Duration.ofSeconds(1));

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).requestWithTimeout(captor.capture(), any(Duration.class));
    assertThat(captor.getValue().getData()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenNoInterceptors_whenRequestObject_thenConnectionReceivesJsonPayload() {
    byte[] json = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
    when(converter.toBytes(any())).thenReturn(json);
    when(connection.requestWithTimeout(any(Message.class), any(Duration.class)))
        .thenReturn(new CompletableFuture<>());
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    client.request("test.subject", new Object(), Duration.ofSeconds(1));

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).requestWithTimeout(captor.capture(), any(Duration.class));
    assertThat(captor.getValue().getData()).isEqualTo(json);
  }

  @Test
  void givenNoInterceptors_whenRequestReturnsReply_thenFutureCompletesWithReply() {
    Message reply = Mockito.mock(Message.class);
    when(connection.requestWithTimeout(any(Message.class), any(Duration.class)))
        .thenReturn(CompletableFuture.completedFuture(reply));
    NatsClient client =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();

    CompletableFuture<NatsReply> future =
        client.request("test.subject", new byte[0], Duration.ofSeconds(1));

    assertThat(future.join().getMessage()).isSameAs(reply);
  }

  @Test
  void givenAbortingInterceptor_whenRequest_thenFutureFailsWithIllegalStateException() {
    NatsPublishInterceptor interceptor = (msg, chain) -> {};
    NatsClient client =
        NatsClient.builder()
            .withConnection(connection)
            .withConverter(converter)
            .addInterceptor(interceptor)
            .build();

    CompletableFuture<NatsReply> future =
        client.request("test.subject", new byte[0], Duration.ofSeconds(1));

    assertThatThrownBy(future::join)
        .isInstanceOf(CompletionException.class)
        .cause()
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("suppressed by interceptor");
    verify(connection, never()).requestWithTimeout(any(Message.class), any(Duration.class));
  }

  @Test
  void givenExistingClient_whenMutate_thenNewClientInheritsConnection() {
    NatsClient original =
        NatsClient.builder().withConnection(connection).withConverter(converter).build();
    Connection newConnection = Mockito.mock(Connection.class);

    NatsClient mutated = original.mutate().withConnection(newConnection).build();
    mutated.publish("test.subject", new byte[0]);

    verify(connection, never()).publish(any(Message.class));
    verify(newConnection).publish(any(Message.class));
  }

  @Test
  void givenInterceptorThatAborts_whenPublish_thenConnectionNeverCalled() {
    NatsPublishInterceptor interceptor = (msg, chain) -> {};
    NatsClient client =
        NatsClient.builder()
            .withConnection(connection)
            .withConverter(converter)
            .addInterceptor(interceptor)
            .build();

    client.publish("test.subject", new byte[0]);

    verify(connection, never()).publish(any(Message.class));
  }
}
