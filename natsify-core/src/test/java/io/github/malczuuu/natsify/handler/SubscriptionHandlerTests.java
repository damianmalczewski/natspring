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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.malczuuu.natsify.core.ListenerConfigureException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SubscriptionHandlerTests {

  private static final Method METHOD;

  static {
    try {
      METHOD = Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private SubscriptionHandler handler;

  @BeforeEach
  void beforeEach() {
    Connection connection = Mockito.mock(Connection.class);
    Dispatcher dispatcher = Mockito.mock(Dispatcher.class);
    when(connection.createDispatcher(any())).thenReturn(dispatcher);

    NatsListenerEndpoint endpoint =
        NatsListenerEndpoint.builder()
            .withBean(new Object())
            .withMethod(METHOD)
            .withSubject("test.subject")
            .withQueue("")
            .build();

    handler = new SubscriptionHandler(connection, endpoint, msg -> {});
  }

  @Test
  void givenAlreadyStarted_whenStartCalledAgain_thenThrowsListenerConfigureException() {
    handler.start();

    assertThatThrownBy(handler::start)
        .isInstanceOf(ListenerConfigureException.class)
        .hasMessageContaining("on already started");
  }

  @Test
  void givenNotStarted_whenStopCalled_thenThrowsListenerConfigureException() {
    assertThatThrownBy(handler::stop)
        .isInstanceOf(ListenerConfigureException.class)
        .hasMessageContaining("on a not-running");
  }

  @Test
  void givenHandler_whenToStringCalled_thenReturnsClassNameWithBeanAndMethod() {
    assertThat(handler.toString()).isEqualTo("SubscriptionHandler[Object.toString]");
  }
}
