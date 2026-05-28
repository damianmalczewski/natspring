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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.malczuuu.natsify.connection.ConnectionSupplier;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.Ordered;
import tools.jackson.databind.json.JsonMapper;

class NatsTemplateTests {

  private ConnectionSupplier connectionSupplier;
  private Connection connection;
  private JsonMapper jsonMapper;

  @BeforeEach
  void beforeEach() {
    connectionSupplier = Mockito.mock(ConnectionSupplier.class);
    connection = Mockito.mock(Connection.class);
    jsonMapper = Mockito.mock(JsonMapper.class);
    when(connectionSupplier.getConnection()).thenReturn(connection);
  }

  @Test
  void givenMissingConnectionSupplier_whenBuild_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(() -> NatsTemplate.builder().withJsonMapper(jsonMapper).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("connectionSupplier");
  }

  @Test
  void givenNoJsonMapper_whenBuild_thenSucceeds() {
    NatsTemplate template =
        NatsTemplate.builder().withConnectionSupplier(connectionSupplier).build();

    template.publish("test.subject", new byte[0]);

    verify(connection).publish(any(Message.class));
  }

  @Test
  void givenSingleInterceptorViaAddInterceptor_whenPublish_thenInterceptorCalled() {
    List<String> calls = new ArrayList<>();
    NatsPublishInterceptor interceptor =
        (msg, chain) -> {
          calls.add("interceptor");
          chain.proceed(msg);
        };
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .addInterceptor(interceptor)
            .build();

    template.publish("test.subject", new byte[0]);

    assertThat(calls).containsExactly("interceptor");
    verify(connection).publish(any(Message.class));
  }

  @Test
  void givenNoInterceptors_whenPublishMessage_thenConnectionReceivesMessage() {
    Message message = NatsMessage.builder().subject("test").build();
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish(message);

    verify(connection).publish(message);
  }

  @Test
  void givenNoInterceptors_whenPublishBytes_thenConnectionReceivesCorrectSubjectAndBody() {
    byte[] body = {1, 2, 3};
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", body);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getSubject()).isEqualTo("test.subject");
    assertThat(captor.getValue().getData()).isEqualTo(body);
  }

  @Test
  void givenNoInterceptors_whenPublishString_thenConnectionReceivesUtf8Bytes() {
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", "hello");

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getData()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenNoInterceptors_whenPublishObject_thenConnectionReceivesJsonBytes() {
    byte[] json = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
    when(jsonMapper.writeValueAsBytes(any())).thenReturn(json);
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", new Object());

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getData()).isEqualTo(json);
  }

  @Test
  void givenNoInterceptors_whenPublishBytesWithHeaders_thenConnectionReceivesHeaders() {
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", headers, new byte[0]);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getHeaders().getFirst("X-Foo")).isEqualTo("bar");
  }

  @Test
  void givenNoInterceptors_whenPublishStringWithHeaders_thenConnectionReceivesHeadersAndUtf8Body() {
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", headers, "hello");

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getHeaders().getFirst("X-Foo")).isEqualTo("bar");
    assertThat(captor.getValue().getData()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void givenNoInterceptors_whenPublishObjectWithHeaders_thenConnectionReceivesHeadersAndJsonBody() {
    byte[] json = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
    when(jsonMapper.writeValueAsBytes(any())).thenReturn(json);
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .build();

    template.publish("test.subject", headers, new Object());

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
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .addInterceptors(List.of(interceptor))
            .build();

    template.publish("test.subject", new byte[0]);

    calls.add("verified");
    verify(connection).publish(any(Message.class));
    assertThat(calls).containsExactly("interceptor", "verified");
  }

  @Test
  void givenTwoInterceptors_whenPublish_thenCalledInOrder() {
    List<String> calls = new ArrayList<>();
    NatsPublishInterceptor first =
        new NatsPublishInterceptor() {
          @Override
          public void intercept(Message msg, NatsPublishInterceptorChain chain) {
            calls.add("first");
            chain.proceed(msg);
          }

          @Override
          public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
          }
        };
    NatsPublishInterceptor second =
        new NatsPublishInterceptor() {
          @Override
          public void intercept(Message msg, NatsPublishInterceptorChain chain) {
            calls.add("second");
            chain.proceed(msg);
          }

          @Override
          public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
          }
        };
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .addInterceptors(List.of(second, first))
            .build();

    template.publish("test.subject", new byte[0]);

    assertThat(calls).containsExactly("first", "second");
  }

  @Test
  void givenInterceptorThatReplacesMessage_whenPublish_thenConnectionReceivesReplacedMessage() {
    Message replacement = NatsMessage.builder().subject("replaced.subject").build();
    NatsPublishInterceptor interceptor = (msg, chain) -> chain.proceed(replacement);
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .addInterceptors(List.of(interceptor))
            .build();

    template.publish("original.subject", new byte[0]);

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(connection).publish(captor.capture());
    assertThat(captor.getValue().getSubject()).isEqualTo("replaced.subject");
  }

  @Test
  void givenInterceptorThatAborts_whenPublish_thenConnectionNeverCalled() {
    NatsPublishInterceptor interceptor = (msg, chain) -> {};
    NatsTemplate template =
        NatsTemplate.builder()
            .withConnectionSupplier(connectionSupplier)
            .withJsonMapper(jsonMapper)
            .addInterceptors(List.of(interceptor))
            .build();

    template.publish("test.subject", new byte[0]);

    verify(connection, never()).publish(any(Message.class));
  }
}
