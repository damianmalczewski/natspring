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
import static org.mockito.Mockito.when;

import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.github.malczuuu.natspring.converter.jackson.JacksonNatsMessageConverter;
import io.nats.client.Message;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.json.JsonMapper;

class SimpleNatsReplyTests {

  private Message message;
  private NatsMessageConverter converter;

  @BeforeEach
  void beforeEach() {
    message = Mockito.mock(Message.class);
    converter = new JacksonNatsMessageConverter(JsonMapper.builder().findAndAddModules().build());
  }

  @Test
  void givenWrappedMessage_whenGetMessage_thenReturnsSameInstance() {
    NatsReply reply = new SimpleNatsReply(message, converter);

    assertThat(reply.getMessage()).isSameAs(message);
  }

  @Test
  void givenJsonBody_whenBodyAsClass_thenDeserializesFromMessageData() {
    when(message.getData()).thenReturn("\"hello\"".getBytes(StandardCharsets.UTF_8));
    NatsReply reply = new SimpleNatsReply(message, converter);

    String result = reply.bodyAs(String.class);

    assertThat(result).isEqualTo("hello");
  }

  @Test
  void givenJsonBody_whenBodyAsTypeReference_thenDeserializesFromMessageData() {
    when(message.getData()).thenReturn("[\"a\",\"b\"]".getBytes(StandardCharsets.UTF_8));
    NatsReply reply = new SimpleNatsReply(message, converter);

    List<String> result = reply.bodyAs(new ParameterizedTypeReference<>() {});

    assertThat(result).containsExactly("a", "b");
  }
}
