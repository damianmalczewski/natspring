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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

/**
 * Default {@link ConnectionHookLifecycle} implementation that invokes registered {@link
 * ConnectionHook} beans when the NATS connection is established or about to be closed.
 *
 * @since 0.2.0
 */
public class ManagedConnectionHookLifecycle implements ConnectionHookLifecycle {

  private static final Logger log = LoggerFactory.getLogger(ManagedConnectionHookLifecycle.class);

  private final Connection connection;
  private final List<ConnectionHook> actions;

  private boolean running = false;

  public ManagedConnectionHookLifecycle(Connection connection, List<ConnectionHook> actions) {
    this.connection = connection;
    this.actions = actions;
  }

  /** Invokes {@link ConnectionHook#postConnect(Connection)} on all registered hooks in order. */
  @Override
  public synchronized void start() {
    for (ConnectionHook hook : actions) {
      log.debug("Triggering connection postConnect hook for {}", AopUtils.getTargetClass(hook));
      hook.postConnect(connection);
    }
    running = true;
  }

  /** Invokes {@link ConnectionHook#preClose(Connection)} on all registered hooks in order. */
  @Override
  public synchronized void stop() {
    for (ConnectionHook hook : actions) {
      log.debug("Triggering connection preClose hook for {}", AopUtils.getTargetClass(hook));
      hook.preClose(connection);
    }
    running = false;
  }

  /** Returns {@code true} after {@link #start()} completes and before {@link #stop()} completes. */
  @Override
  public boolean isRunning() {
    return running;
  }
}
