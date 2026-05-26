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

package io.github.malczuuu.natsify.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NatsListenerInvocationTests {

  MessageArgumentResolver argumentResolver;
  Message message;
  NatsListenerObserver observer;
  Connection connection;

  private Listener listener;

  @BeforeEach
  void setUp() {
    argumentResolver = Mockito.mock(MessageArgumentResolver.class);
    message = Mockito.mock(Message.class);
    observer = Mockito.mock(NatsListenerObserver.class);
    connection = Mockito.mock(Connection.class);
    listener = new Listener();
  }

  @Test
  void whenHandlerSucceeds_thenCallsOnSucceeded() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handle"))
        .accept(message);

    verify(observer).onSucceeded("test-subject", "");
    verify(observer, never()).onFailed(any(), any());
    assertThat(listener.called).isTrue();
  }

  @Test
  void whenHandlerSucceeds_thenCallsOnReceivedAndOnProcessed() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handle"))
        .accept(message);

    verify(observer).onReceived("test-subject", "");
    verify(observer).onProcessed(eq("test-subject"), eq(""), anyLong());
  }

  @Test
  void whenHandlerThrows_thenCallsOnFailed() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handleThrowing"))
        .accept(message);

    verify(observer).onFailed("test-subject", "");
    verify(observer, never()).onSucceeded(any(), any());
  }

  @Test
  void whenHandlerThrows_thenOnProcessedStillFires() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handleThrowing"))
        .accept(message);

    verify(observer).onReceived("test-subject", "");
    verify(observer).onProcessed(eq("test-subject"), eq(""), anyLong());
  }

  @Test
  void whenResolverThrows_thenCallsOnFailedAndHandlerIsNotCalled() {
    when(argumentResolver.resolveArguments(any(), any()))
        .thenThrow(new RuntimeException("bad payload"));

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handle"))
        .accept(message);

    verify(observer).onFailed("test-subject", "");
    verify(observer, never()).onSucceeded(any(), any());
    assertThat(listener.called).isFalse();
  }

  @Test
  void whenResolverThrows_thenOnProcessedStillFires() {
    when(argumentResolver.resolveArguments(any(), any()))
        .thenThrow(new RuntimeException("bad payload"));

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handle"))
        .accept(message);

    verify(observer).onReceived("test-subject", "");
    verify(observer).onProcessed(eq("test-subject"), eq(""), anyLong());
  }

  @Test
  void givenQueueGroup_whenHandlerSucceeds_thenObserverReceivesQueue() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(
            connection, argumentResolver, observer, handleWithQueue("handle", "my-queue"))
        .accept(message);

    verify(observer).onReceived("test-subject", "my-queue");
    verify(observer).onSucceeded("test-subject", "my-queue");
    verify(observer).onProcessed(eq("test-subject"), eq("my-queue"), anyLong());
  }

  @Test
  void givenDeadLetterSubject_whenHandlerThrows_thenPublishesToDeadLetterAndCallsOnDeadLettered() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(
            connection,
            argumentResolver,
            observer,
            handleWithDeadLetter("handleThrowing", "test-subject.dlq"))
        .accept(message);

    verify(connection).publish(any(Message.class));
    verify(observer).onDeadLettered("test-subject", "");
  }

  @Test
  void givenDeadLetterSubject_whenResolverThrows_thenPublishesToDeadLetterAndCallsOnDeadLettered() {
    when(argumentResolver.resolveArguments(any(), any()))
        .thenThrow(new RuntimeException("bad payload"));

    new NatsListenerInvocation(
            connection,
            argumentResolver,
            observer,
            handleWithDeadLetter("handle", "test-subject.dlq"))
        .accept(message);

    verify(connection).publish(any(Message.class));
    verify(observer).onDeadLettered("test-subject", "");
  }

  @Test
  void givenInvocation_whenToStringCalled_thenReturnsClassNameWithBeanAndMethod() {
    NatsListenerInvocation invocation =
        new NatsListenerInvocation(connection, argumentResolver, observer, handle("handle"));

    assertThat(invocation.toString()).isEqualTo("NatsListenerInvocation[Listener.handle]");
  }

  @Test
  void givenNoDeadLetterSubject_whenHandlerThrows_thenDoesNotPublish() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    new NatsListenerInvocation(connection, argumentResolver, observer, handle("handleThrowing"))
        .accept(message);

    verify(connection, never()).publish(any(Message.class));
    verify(observer, never()).onDeadLettered(any(), any());
  }

  private NatsListenerDetails handle(String methodName) {
    return handleWithQueue(methodName, "");
  }

  private NatsListenerDetails handleWithQueue(String methodName, String queue) {
    return handleWithQueueAndDeadLetter(methodName, queue, "");
  }

  private NatsListenerDetails handleWithDeadLetter(String methodName, String deadLetterSubject) {
    return handleWithQueueAndDeadLetter(methodName, "", deadLetterSubject);
  }

  private NatsListenerDetails handleWithQueueAndDeadLetter(
      String methodName, String queue, String deadLetterSubject) {
    try {
      Method method = Listener.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return NatsListenerDetails.builder()
          .withBean(listener)
          .withMethod(method)
          .withSubject("test-subject")
          .withQueue(queue)
          .withDeadLetterSubject(deadLetterSubject)
          .build();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  static class Listener {

    boolean called = false;

    void handle() {
      called = true;
    }

    void handleThrowing() {
      throw new RuntimeException("boom");
    }
  }
}
