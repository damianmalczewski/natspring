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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe {@link NatsListenerRegistry} backed by a {@link CopyOnWriteArrayList}.
 *
 * @since 0.1.0
 */
public final class SimpleNatsListenerRegistry implements NatsListenerRegistry {

  private final List<NatsListenerDetails> listeners = new CopyOnWriteArrayList<>();

  /**
   * Creates a new {@code SimpleNatsListenerRegistry}.
   *
   * @since 0.1.0
   */
  public SimpleNatsListenerRegistry() {}

  /**
   * Registers a listener. Also marks the listener method as accessible to support non-public
   * methods.
   *
   * @param listener the listener details to register
   * @since 0.1.0
   */
  @Override
  public void register(NatsListenerDetails listener) {
    listener.getMethod().setAccessible(true);
    listeners.add(listener);
  }

  /**
   * Returns all registered listeners.
   *
   * @return immutable list of registered listener details
   * @since 0.1.0
   */
  @Override
  public List<NatsListenerDetails> getListeners() {
    return Collections.unmodifiableList(listeners);
  }
}
