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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.malczuuu.natspring.core.NatsMessageInterceptor;
import io.nats.client.Message;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NatsMessageInterceptorChainExecutionTests {

  @Test
  void givenNoInterceptors_whenExecuted_thenTargetIsCalled() {
    Message message = Mockito.mock(Message.class);
    List<Message> received = new ArrayList<>();

    new NatsMessageInterceptorChainExecution(List.of()).execute(message, received::add);

    assertThat(received).containsExactly(message);
  }

  @Test
  void givenOneInterceptor_whenExecuted_thenInterceptorAndTargetAreCalled() {
    Message message = Mockito.mock(Message.class);
    List<String> calls = new ArrayList<>();

    NatsMessageInterceptor interceptor =
        (msg, chain) -> {
          calls.add("before");
          chain.proceed(msg);
          calls.add("after");
        };

    new NatsMessageInterceptorChainExecution(List.of(interceptor))
        .execute(message, m -> calls.add("target"));

    assertThat(calls).containsExactly("before", "target", "after");
  }

  @Test
  void givenTwoInterceptors_whenExecuted_thenCalledInOrder() {
    Message message = Mockito.mock(Message.class);
    List<String> calls = new ArrayList<>();

    NatsMessageInterceptor first =
        (msg, chain) -> {
          calls.add("first-before");
          chain.proceed(msg);
          calls.add("first-after");
        };
    NatsMessageInterceptor second =
        (msg, chain) -> {
          calls.add("second-before");
          chain.proceed(msg);
          calls.add("second-after");
        };

    new NatsMessageInterceptorChainExecution(List.of(first, second))
        .execute(message, m -> calls.add("target"));

    assertThat(calls)
        .containsExactly("first-before", "second-before", "target", "second-after", "first-after");
  }

  @Test
  void givenInterceptorThatAborts_whenExecuted_thenTargetIsNotCalled() {
    Message message = Mockito.mock(Message.class);
    List<Message> received = new ArrayList<>();

    NatsMessageInterceptor aborting = (msg, chain) -> {};

    new NatsMessageInterceptorChainExecution(List.of(aborting)).execute(message, received::add);

    assertThat(received).isEmpty();
  }

  @Test
  void givenInterceptorThatReplacesMessage_whenExecuted_thenTargetReceivesReplacedMessage() {
    Message original = Mockito.mock(Message.class);
    Message replacement = Mockito.mock(Message.class);
    List<Message> received = new ArrayList<>();

    NatsMessageInterceptor replacing = (msg, chain) -> chain.proceed(replacement);

    new NatsMessageInterceptorChainExecution(List.of(replacing)).execute(original, received::add);

    assertThat(received).containsExactly(replacement);
  }

  @Test
  void givenInterceptorThatThrows_whenExecuted_thenExceptionPropagates() {
    Message message = Mockito.mock(Message.class);
    RuntimeException boom = new RuntimeException("boom");

    NatsMessageInterceptor throwing =
        (msg, chain) -> {
          throw boom;
        };

    assertThatThrownBy(
            () ->
                new NatsMessageInterceptorChainExecution(List.of(throwing))
                    .execute(message, m -> {}))
        .isSameAs(boom);
  }
}
