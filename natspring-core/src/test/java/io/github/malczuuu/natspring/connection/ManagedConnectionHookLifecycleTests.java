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

package io.github.malczuuu.natspring.connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.nats.client.Connection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class ManagedConnectionHookLifecycleTests {

  private Connection connection;

  @BeforeEach
  void beforeEach() {
    connection = Mockito.mock(Connection.class);
  }

  @Test
  void givenFreshInstance_whenIsRunning_thenReturnsFalse() {
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of());

    assertThat(lifecycle.isRunning()).isFalse();
  }

  @Test
  void givenNoHooks_whenStart_thenIsRunning() {
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of());

    lifecycle.start();

    assertThat(lifecycle.isRunning()).isTrue();
  }

  @Test
  void givenNoHooks_whenStop_thenNotRunning() {
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of());
    lifecycle.start();

    lifecycle.stop();

    assertThat(lifecycle.isRunning()).isFalse();
  }

  @Test
  void givenSingleHook_whenStart_thenPostConnectCalled() {
    ConnectionHook hook = Mockito.mock(ConnectionHook.class);
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of(hook));

    lifecycle.start();

    verify(hook).postConnect(connection);
    verify(hook, never()).preClose(connection);
  }

  @Test
  void givenSingleHook_whenStop_thenPreCloseCalled() {
    ConnectionHook hook = Mockito.mock(ConnectionHook.class);
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of(hook));

    lifecycle.stop();

    verify(hook).preClose(connection);
    verify(hook, never()).postConnect(connection);
  }

  @Test
  void givenMultipleHooks_whenStart_thenPostConnectCalledOnEachInOrder() {
    ConnectionHook first = Mockito.mock(ConnectionHook.class);
    ConnectionHook second = Mockito.mock(ConnectionHook.class);
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of(first, second));

    lifecycle.start();

    InOrder order = inOrder(first, second);
    order.verify(first).postConnect(connection);
    order.verify(second).postConnect(connection);
  }

  @Test
  void givenMultipleHooks_whenStop_thenPreCloseCalledOnEachInOrder() {
    ConnectionHook first = Mockito.mock(ConnectionHook.class);
    ConnectionHook second = Mockito.mock(ConnectionHook.class);
    ManagedConnectionHookLifecycle lifecycle =
        new ManagedConnectionHookLifecycle(connection, List.of(first, second));

    lifecycle.stop();

    InOrder order = inOrder(first, second);
    order.verify(first).preClose(connection);
    order.verify(second).preClose(connection);
  }
}
