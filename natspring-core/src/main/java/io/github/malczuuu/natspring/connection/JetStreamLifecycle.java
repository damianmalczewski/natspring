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

import org.springframework.context.SmartLifecycle;

/**
 * {@link SmartLifecycle} marker interface for JetStream lifecycle beans.
 *
 * @since 0.1.0
 */
public interface JetStreamLifecycle extends SmartLifecycle {

  /**
   * Returns the phase for JetStream lifecycle beans, which starts after {@link ConnectionLifecycle}
   * and before {@link ListenerContainerLifecycle}.
   *
   * @return this lifecycle phase
   */
  @Override
  default int getPhase() {
    return ConnectionLifecycle.CONNECTION_LIFECYCLE_PHASE + 50;
  }
}
