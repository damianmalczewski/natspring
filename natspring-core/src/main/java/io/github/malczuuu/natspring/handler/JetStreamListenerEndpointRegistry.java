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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry for {@link JetStreamListenerEndpoint} instances backed by a {@link
 * CopyOnWriteArrayList}.
 *
 * @since 0.1.0
 */
public class JetStreamListenerEndpointRegistry {

  private final List<JetStreamListenerEndpoint> endpoints = new CopyOnWriteArrayList<>();

  /**
   * Creates a new {@code JetStreamListenerEndpointRegistry}.
   *
   * @since 0.1.0
   */
  public JetStreamListenerEndpointRegistry() {}

  /**
   * Registers a listener endpoint. Also marks the listener method as accessible to support
   * non-public methods.
   *
   * @param endpoint the listener endpoint to register
   * @since 0.1.0
   */
  public void register(JetStreamListenerEndpoint endpoint) {
    endpoint.getMethod().setAccessible(true);
    endpoints.add(endpoint);
  }

  /**
   * Returns all registered listeners.
   *
   * @return immutable list of registered listener endpoints
   * @since 0.1.0
   */
  public List<JetStreamListenerEndpoint> getEndpoints() {
    return Collections.unmodifiableList(endpoints);
  }
}
