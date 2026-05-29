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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.malczuuu.natspring.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.NatsMessage;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.json.JsonMapper;

class NatsListenerInvocationTests {

  private final MessageArgumentResolver argumentResolver =
      new SimpleMessageArgumentResolver(JsonMapper.builder().findAndAddModules().build());

  private Connection connection;
  private Listener listener;

  @BeforeEach
  void beforeEach() {
    connection = Mockito.mock(Connection.class);
    listener = new Listener();
  }

  @Test
  void givenNoArgsHandler_whenMessageReceived_thenHandlerIsCalled() {
    Message message = NatsMessage.builder().subject("test-subject").build();

    invocation(handle("handle")).accept(message);

    assertThat(listener.called).isTrue();
  }

  @Test
  void givenHandlerThatThrows_whenMessageReceived_thenAcceptDoesNotThrow() {
    Message message = NatsMessage.builder().subject("test-subject").build();

    invocation(handle("handleThrowing")).accept(message);

    assertThat(listener.throwingCalled).isTrue();
  }

  @Test
  void givenInvalidPayload_whenMessageReceived_thenHandlerIsNotCalled() {
    Message message =
        NatsMessage.builder()
            .subject("test-subject")
            .data("not-valid-json".getBytes(StandardCharsets.UTF_8))
            .build();

    invocation(handle("handleObject")).accept(message);

    assertThat(listener.called).isFalse();
  }

  @Test
  void givenDeadLetterSubject_whenResolverThrows_thenPublishesToDeadLetter() {
    Message message =
        NatsMessage.builder()
            .subject("test-subject")
            .data("not-valid-json".getBytes(StandardCharsets.UTF_8))
            .build();

    invocation(handleWithDeadLetter("handleObject", "test-subject.dlq")).accept(message);

    verify(connection).publish(any(Message.class));
  }

  @Test
  void givenDeadLetterSubject_whenHandlerThrows_thenPublishesToDeadLetter() {
    Message message = NatsMessage.builder().subject("test-subject").build();

    invocation(handleWithDeadLetter("handleThrowing", "test-subject.dlq")).accept(message);

    verify(connection).publish(any(Message.class));
  }

  @Test
  void givenNoDeadLetterSubject_whenHandlerThrows_thenDoesNotPublish() {
    Message message = NatsMessage.builder().subject("test-subject").build();

    invocation(handle("handleThrowing")).accept(message);

    verify(connection, never()).publish(any(Message.class));
  }

  @Test
  void givenInvocation_whenToStringCalled_thenReturnsClassNameWithBeanAndMethod() {
    NatsListenerInvocation invocation = invocation(handle("handle"));

    assertThat(invocation.toString()).isEqualTo("NatsListenerInvocation[Listener.handle]");
  }

  private NatsListenerEndpoint handle(String methodName) {
    return handleWithQueue(methodName, "");
  }

  private NatsListenerEndpoint handleWithQueue(String methodName, String queue) {
    return handleWithQueueAndDeadLetter(methodName, queue, "");
  }

  private NatsListenerEndpoint handleWithDeadLetter(String methodName, String deadLetterSubject) {
    return handleWithQueueAndDeadLetter(methodName, "", deadLetterSubject);
  }

  private NatsListenerEndpoint handleWithQueueAndDeadLetter(
      String methodName, String queue, String deadLetterSubject) {
    Method method =
        Arrays.stream(Listener.class.getDeclaredMethods())
            .filter(m -> m.getName().equals(methodName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("method not found: " + methodName));
    method.setAccessible(true);
    return NatsListenerEndpoint.builder()
        .withBean(listener)
        .withMethod(method)
        .withSubject("test-subject")
        .withQueue(queue)
        .withDeadLetterSubject(deadLetterSubject)
        .build();
  }

  private NatsListenerInvocation invocation(NatsListenerEndpoint endpoint) {
    return new NatsListenerInvocation(
        connection, argumentResolver, NatsListenerObserver.noop(), endpoint, List.of());
  }

  @SuppressWarnings("unused")
  static class Listener {

    boolean called = false;
    boolean throwingCalled = false;

    void handle() {
      called = true;
    }

    void handleThrowing() {
      throwingCalled = true;
      throw new RuntimeException("boom");
    }

    void handleObject(SomePayload payload) {
      called = true;
    }
  }

  record SomePayload(String value) {}
}
