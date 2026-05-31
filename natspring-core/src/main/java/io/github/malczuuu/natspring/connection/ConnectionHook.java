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

import io.nats.client.Connection;

/**
 * Callback interface for receiving notifications when the NATS connection is established or about
 * to be closed.
 *
 * <p>Implement this interface and register the implementation as a Spring bean to run custom logic
 * tied to the lifecycle of the underlying {@link Connection}. A typical use case is setting up or
 * tearing down resources that depend directly on the native connection - for example, custom
 * subscriptions or dispatchers - which must be recreated each time the connection is started.
 *
 * @see ManagedConnectionHookLifecycle
 * @since 0.2.0
 */
public interface ConnectionHook {

  /**
   * Invoked after the NATS connection has been fully established.
   *
   * @param connection the active NATS connection
   */
  default void postConnect(Connection connection) {}

  /**
   * Invoked before the NATS connection is closed.
   *
   * <p>The connection is still open and operational at the time this method is called.
   *
   * @param connection the active NATS connection
   */
  default void preClose(Connection connection) {}
}
