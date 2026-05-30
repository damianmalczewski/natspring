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

package io.github.malczuuu.natspring.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DeadLetterSupportTests {

  private Connection connection;

  @BeforeEach
  void beforeEach() {
    connection = Mockito.mock(Connection.class);
  }

  @Test
  void givenNoCause_whenBuildDeadLetterHeaders_thenOnlySubjectAndTimestampPresent() {
    Message message = NatsMessage.builder().subject("src.subject").build();

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", null);

    assertThat(headers.getFirst("X-Dead-Letter-Subject")).isEqualTo("src.subject");
    assertThat(headers.get("X-Dead-Letter-Reason")).isNull();
    assertThat(headers.get("X-Dead-Letter-Exception")).isNull();
    assertThat(headers.getFirst("X-Dead-Letter-Timestamp")).isNotNull();
  }

  @Test
  void givenCause_whenBuildDeadLetterHeaders_thenReasonAndExceptionHeadersPresent() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    RuntimeException cause = new RuntimeException("something went wrong");

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", cause);

    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("RuntimeException");
    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("something went wrong");
    assertThat(headers.getFirst("X-Dead-Letter-Exception")).isEqualTo("java.lang.RuntimeException");
  }

  @Test
  void givenCauseWithNewlineInMessage_whenBuildDeadLetterHeaders_thenNewlinesSanitized() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    RuntimeException cause = new RuntimeException("line one\nline two\r\nline three");

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", cause);

    assertThat(headers.getFirst("X-Dead-Letter-Reason"))
        .isEqualTo("RuntimeException: line one line two  line three");
  }

  @Test
  void givenInvocationTargetException_whenBuildDeadLetterHeaders_thenRootCauseUsed() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    IllegalStateException root = new IllegalStateException("root cause");
    InvocationTargetException cause = new InvocationTargetException(root);

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", cause);

    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("IllegalStateException");
    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("root cause");
    assertThat(headers.getFirst("X-Dead-Letter-Exception"))
        .isEqualTo("java.lang.IllegalStateException");
  }

  @Test
  void givenError_whenBuildDeadLetterHeaders_thenReasonAndExceptionHeadersPresent() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    StackOverflowError cause = new StackOverflowError("stack overflow");

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", cause);

    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("StackOverflowError");
    assertThat(headers.getFirst("X-Dead-Letter-Reason")).contains("stack overflow");
    assertThat(headers.getFirst("X-Dead-Letter-Exception"))
        .isEqualTo("java.lang.StackOverflowError");
  }

  @Test
  void givenCauseWithLongMessage_whenBuildDeadLetterHeaders_thenReasonTruncated() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    String longMessage = "x".repeat(300);
    RuntimeException cause = new RuntimeException(longMessage);

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", cause);

    assertThat(headers.getFirst("X-Dead-Letter-Reason")).endsWith("...").hasSizeLessThan(300);
  }

  @Test
  void givenOriginalMessageWithHeaders_whenBuildDeadLetterHeaders_thenHeadersCopied() {
    Headers origHeaders = new Headers();
    origHeaders.add("X-Trace-Id", "abc123");
    Message message = NatsMessage.builder().subject("src.subject").headers(origHeaders).build();

    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", null);

    assertThat(headers.getFirst("X-Trace-Id")).isEqualTo("abc123");
  }

  @Test
  void givenValidMessage_whenBuildAndPublishDeadLetter_thenConnectionPublishes() {
    Message message =
        NatsMessage.builder()
            .subject("src.subject")
            .data("payload".getBytes(StandardCharsets.UTF_8))
            .build();
    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", null);

    DeadLetterSupport.buildAndPublishDeadLetter(connection, "src.subject.dlq", message, headers);

    verify(connection).publish(any(Message.class));
  }

  @Test
  void givenMessageWithNullData_whenBuildAndPublishDeadLetter_thenPublishesEmptyBody() {
    Message message = NatsMessage.builder().subject("src.subject").build();
    Headers headers = DeadLetterSupport.buildDeadLetterHeaders(message, "src.subject", null);

    DeadLetterSupport.buildAndPublishDeadLetter(connection, "src.subject.dlq", message, headers);

    verify(connection).publish(any(Message.class));
  }
}
