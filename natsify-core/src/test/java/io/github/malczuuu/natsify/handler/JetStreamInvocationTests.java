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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.malczuuu.natsify.annotation.AckMode;
import io.github.malczuuu.natsify.annotation.ConsumerType;
import io.github.malczuuu.natsify.annotation.DeliverPolicyType;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JetStreamInvocationTests {

  MessageArgumentResolver argumentResolver;
  Message message;
  Connection connection;

  private Listener listener;

  @BeforeEach
  void beforeEach() {
    argumentResolver = Mockito.mock(MessageArgumentResolver.class);
    message = Mockito.mock(Message.class);
    connection = Mockito.mock(Connection.class);
    listener = new Listener();
  }

  @Test
  void givenAutoAckMode_whenHandlerSucceeds_thenAcks() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    invocation(endpoint("handle", AckMode.AUTO)).accept(message);

    verify(message).ack();
    verify(message, never()).nak();
    assertThat(listener.called).isTrue();
  }

  @Test
  void givenManualAckMode_whenHandlerSucceeds_thenNoAckNorNak() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    invocation(endpoint("handle", AckMode.MANUAL)).accept(message);

    verify(message, never()).ack();
    verify(message, never()).nak();
  }

  @Test
  void givenAutoAckMode_whenHandlerThrows_thenNaks() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    invocation(endpoint("handleThrowing", AckMode.AUTO)).accept(message);

    verify(message).nak();
    verify(message, never()).ack();
  }

  @Test
  void givenManualAckMode_whenHandlerThrows_thenNoNakNorAck() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);

    invocation(endpoint("handleThrowing", AckMode.MANUAL)).accept(message);

    verify(message, never()).nak();
    verify(message, never()).ack();
  }

  @Test
  void givenResolverThrows_whenInvoked_thenTerminates() {
    when(argumentResolver.resolveArguments(any(), any()))
        .thenThrow(new RuntimeException("bad payload"));

    invocation(endpoint("handle", AckMode.AUTO)).accept(message);

    verify(message).term();
    verify(message, never()).ack();
    assertThat(listener.called).isFalse();
  }

  @Test
  void givenInvocation_whenToStringCalled_thenReturnsClassNameWithBeanAndMethod() {
    JetStreamInvocation invocation = invocation(endpoint("handle", AckMode.AUTO));

    assertThat(invocation.toString()).isEqualTo("JetStreamInvocation[Listener.handle]");
  }

  @Test
  void givenDlqConfigured_whenHandlerThrowsOnLastDelivery_thenTermsAndPublishesToDlq() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);
    NatsJetStreamMetaData meta = Mockito.mock(NatsJetStreamMetaData.class);
    when(meta.deliveredCount()).thenReturn(3L);
    when(message.metaData()).thenReturn(meta);

    JetStreamListenerObserver observer = Mockito.mock(JetStreamListenerObserver.class);
    invocation(endpointWithDlq("handleThrowing", "dlq.subject", 3), observer).accept(message);

    verify(message).term();
    verify(message, never()).nak();
    verify(connection).publish(any(Message.class));
    verify(observer).onDeadLettered("test-subject", "");
  }

  @Test
  void givenDlqConfigured_whenHandlerThrowsBeforeLastDelivery_thenNaks() {
    when(argumentResolver.resolveArguments(any(), any())).thenReturn(new Object[0]);
    NatsJetStreamMetaData meta = Mockito.mock(NatsJetStreamMetaData.class);
    when(meta.deliveredCount()).thenReturn(2L);
    when(message.metaData()).thenReturn(meta);

    invocation(endpointWithDlq("handleThrowing", "dlq.subject", 3)).accept(message);

    verify(message).nak();
    verify(message, never()).term();
    verify(connection, never()).publish(any(Message.class));
  }

  @Test
  void givenDlqConfigured_whenResolverThrows_thenTermsAndPublishesToDlq() {
    when(argumentResolver.resolveArguments(any(), any()))
        .thenThrow(new RuntimeException("bad payload"));

    JetStreamListenerObserver observer = Mockito.mock(JetStreamListenerObserver.class);
    invocation(endpointWithDlq("handle", "dlq.subject", 3), observer).accept(message);

    verify(message).term();
    verify(connection).publish(any(Message.class));
    verify(observer).onDeadLettered("test-subject", "");
  }

  private JetStreamInvocation invocation(JetStreamListenerEndpoint details) {
    return invocation(details, JetStreamListenerObserver.noop());
  }

  private JetStreamInvocation invocation(
      JetStreamListenerEndpoint details, JetStreamListenerObserver observer) {
    return new JetStreamInvocation(connection, argumentResolver, observer, details, List.of());
  }

  private JetStreamListenerEndpoint endpoint(String methodName, AckMode ackMode) {
    try {
      Method method = Listener.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return JetStreamListenerEndpoint.builder()
          .withBean(listener)
          .withMethod(method)
          .withSubject("test-subject")
          .withStream("")
          .withDurable("")
          .withQueue("")
          .withConsumerType(ConsumerType.PUSH)
          .withAckMode(ackMode)
          .withDeliverPolicy(DeliverPolicyType.NEW)
          .build();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private JetStreamListenerEndpoint endpointWithDlq(
      String methodName, String dlqSubject, int maxDeliveries) {
    try {
      Method method = Listener.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return JetStreamListenerEndpoint.builder()
          .withBean(listener)
          .withMethod(method)
          .withSubject("test-subject")
          .withStream("")
          .withDurable("")
          .withQueue("")
          .withConsumerType(ConsumerType.PUSH)
          .withAckMode(AckMode.AUTO)
          .withDeliverPolicy(DeliverPolicyType.NEW)
          .withDeadLetterSubject(dlqSubject)
          .withMaxDeliveries(maxDeliveries)
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
