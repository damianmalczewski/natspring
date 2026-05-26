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

import io.github.malczuuu.natsify.annotation.NatsListener;
import java.util.List;

/**
 * Registry for {@link NatsListenerDetails} describing {@link NatsListener @NatsListener}-annotated
 * methods.
 *
 * @since 0.1.0
 */
public interface NatsListenerRegistry {

  /**
   * Registers a listener.
   *
   * @param listener the listener details to register
   * @since 0.1.0
   */
  void register(NatsListenerDetails listener);

  /**
   * Returns all registered listeners.
   *
   * @return immutable list of registered listener details
   * @since 0.1.0
   */
  List<NatsListenerDetails> getListeners();
}
