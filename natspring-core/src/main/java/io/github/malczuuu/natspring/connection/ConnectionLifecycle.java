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
import org.springframework.context.SmartLifecycle;

/**
 * {@link SmartLifecycle} extension of the NATS {@link Connection} interface, allowing the
 * connection to be managed as a Spring lifecycle bean.
 *
 * @since 0.1.0
 */
public interface ConnectionLifecycle extends Connection, SmartLifecycle {

  /**
   * Phase at which connection lifecycle beans start, before JetStream and listener containers.
   *
   * <p>Configured as before {@code WebServerApplicationContext.START_STOP_LIFECYCLE_PHASE}, so NATS
   * connections are started before web server is started. This way NATS health is available
   * immediately when the application is ready to serve requests.
   *
   * @see <a
   *     href="https://docs.spring.io/spring-boot/api/java/org/springframework/boot/web/server/context/WebServerApplicationContext.html">
   *     {@code WebServerApplicationContext}</a>
   */
  int CONNECTION_LIFECYCLE_PHASE = SmartLifecycle.DEFAULT_PHASE - 3 * 1024;

  /**
   * Returns the phase for connection lifecycle beans, which starts before {@link
   * JetStreamLifecycle} and {@link ListenerContainerLifecycle}.
   *
   * @return this lifecycle phase
   */
  @Override
  default int getPhase() {
    return CONNECTION_LIFECYCLE_PHASE;
  }
}
