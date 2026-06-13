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

import io.github.malczuuu.natspring.core.NatsIntegrationException;
import io.github.malczuuu.natspring.core.NatsListenerMethodException;
import io.github.malczuuu.natspring.handler.MessageListenerContainer;
import io.nats.client.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.SmartLifecycle;

/**
 * {@link SmartLifecycle} bean that starts and stops registered {@link MessageListenerContainer}
 * instances against the provided NATS {@link Connection}.
 *
 * @since 0.1.0
 * @see ManagedConnectionLifecycle
 * @see ManagedJetStreamLifecycle
 */
public class ManagedListenerContainerLifecycle implements ListenerContainerLifecycle {

  private static final Logger log =
      LoggerFactory.getLogger(ManagedListenerContainerLifecycle.class);

  private final Connection connection;
  private final List<MessageListenerContainer> containers;

  private volatile boolean running = false;

  /**
   * Creates a new {@link ManagedListenerContainerLifecycle}.
   *
   * @param connection the active NATS connection used to start each container
   * @param containers listener containers to manage
   */
  public ManagedListenerContainerLifecycle(
      Connection connection, List<MessageListenerContainer> containers) {
    this.connection = connection;
    this.containers = List.copyOf(containers);
  }

  /** Starts all registered listener containers against the active NATS connection. */
  @Override
  public synchronized void start() {
    int started = 0;
    try {
      for (MessageListenerContainer container : containers) {
        if (container.isEmpty()) {
          continue;
        }
        log.info(
            "Setting up annotation-based NATS listeners for type={}",
            AopUtils.getTargetClass(container).getSimpleName());
        container.start(connection);
        started++;
      }
    } catch (Exception e) {
      for (int i = started - 1; i >= 0; i--) {
        try {
          containers.get(i).stop();
        } catch (Exception suppressed) {
          e.addSuppressed(suppressed);
        }
      }
      if (e instanceof NatsIntegrationException ex) {
        throw ex;
      }
      throw new NatsListenerMethodException("Failed to set up annotation-based NATS listeners", e);
    }
    running = true;
  }

  /** Stops all registered listener containers. */
  @Override
  public synchronized void stop() {
    running = false;
    for (MessageListenerContainer container : containers) {
      if (container.isEmpty()) {
        continue;
      }
      log.info(
          "Shutting down annotation-based NATS listeners for type={}",
          AopUtils.getTargetClass(container).getSimpleName());
      container.stop();
    }
  }

  /**
   * Returns {@code true} if all listener containers have been started.
   *
   * @return {@code true} if running
   */
  @Override
  public boolean isRunning() {
    return running;
  }
}
