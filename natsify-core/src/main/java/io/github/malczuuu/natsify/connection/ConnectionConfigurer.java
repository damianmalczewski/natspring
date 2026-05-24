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

package io.github.malczuuu.natsify.connection;

import io.github.malczuuu.natsify.core.ConnectionException;
import io.github.malczuuu.natsify.core.ListenerConfigureException;
import io.github.malczuuu.natsify.core.NatsIntegrationException;
import io.github.malczuuu.natsify.handler.ListenerManager;
import io.github.malczuuu.natsify.instrument.NatsConnectionObserver;
import io.github.malczuuu.natsify.instrument.NatsErrorObserver;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

/**
 * {@link ConnectionManager} that establishes and manages a NATS {@link Connection} as a Spring
 * lifecycle bean, and initializes annotation-based listeners on startup.
 *
 * <p>Also implements {@link ConnectionListener} and {@link ErrorListener} to forward connection
 * events and errors to instrumentation observers.
 */
public final class ConnectionConfigurer
    implements ConnectionManager, ConnectionListener, ErrorListener {

  private static final Logger log = LoggerFactory.getLogger(ConnectionConfigurer.class);

  private final Options options;
  private final List<? extends ListenerManager> listenerManagers;
  private final NatsConnectionObserver natsConnectionObserver;
  private final NatsErrorObserver natsErrorObserver;

  private volatile @Nullable Connection connection = null;

  /**
   * Creates a new {@link ConnectionConfigurer}.
   *
   * @param options NATS connection options
   * @param listenerManagers managers responsible for setting up annotation-based listeners
   * @param natsConnectionObserver observer for connection lifecycle events
   * @param natsErrorObserver observer for errors and exceptions
   */
  public ConnectionConfigurer(
      Options options,
      List<? extends ListenerManager> listenerManagers,
      NatsConnectionObserver natsConnectionObserver,
      NatsErrorObserver natsErrorObserver) {
    this.options = options;
    this.listenerManagers = List.copyOf(listenerManagers);
    this.natsConnectionObserver = natsConnectionObserver;
    this.natsErrorObserver = natsErrorObserver;
  }

  /**
   * Returns the active NATS connection, establishing one if not yet connected.
   *
   * @return the active {@link Connection}
   */
  @Override
  public Connection getConnection() {
    if (connection == null) {
      synchronized (this) {
        if (connection == null) {
          try {
            log.info("Establishing NATS connection at {}", options.getServers());
            this.connection = Nats.connect(options);
          } catch (Exception e) {
            throw new ConnectionException("Failed to establish NATS connection", e);
          }
        }
      }
    }
    return Objects.requireNonNull(connection);
  }

  /** Establishes the NATS connection and initializes all registered listener managers. */
  @Override
  public synchronized void start() {
    Connection connection = getConnection();
    try {
      for (ListenerManager listenerManager : listenerManagers) {
        log.info(
            "Setting up annotation-based NATS listeners with {}",
            AopUtils.getTargetClass(listenerManager).getSimpleName());
        listenerManager.initialize(connection);
      }
    } catch (Exception e) {
      if (e instanceof NatsIntegrationException ex) {
        throw ex;
      }
      throw new ListenerConfigureException("Failed to set up annotation-based NATS listeners", e);
    }
  }

  /** Stops all listener managers in reverse registration order and closes the NATS connection. */
  @Override
  public synchronized void stop() {
    List<? extends ListenerManager> listenerManagers = new ArrayList<>(this.listenerManagers);
    Collections.reverse(listenerManagers);
    for (ListenerManager listenerManager : listenerManagers) {
      log.info(
          "Shutting down annotation-based NATS listeners with {}",
          AopUtils.getTargetClass(listenerManager).getSimpleName());
      listenerManager.stop();
    }

    log.info("Closing NATS connection at {}", options.getServers());
    try {
      Connection connection = this.connection;
      if (connection != null) {
        connection.close();
      }
    } catch (Exception e) {
      if (e instanceof NatsIntegrationException ex) {
        throw ex;
      }
      throw new ListenerConfigureException("Failed to close NATS connection", e);
    } finally {
      this.connection = null;
    }
  }

  /**
   * Returns {@code true} if the NATS connection has been established and not yet closed.
   *
   * @return {@code true} if connected
   */
  @Override
  public boolean isRunning() {
    return connection != null;
  }

  /**
   * Need to override due to contract from {@link ConnectionListener}. Method is deprecated and
   * never used. NATS internals call {@link #connectionEvent(Connection, Events, Long, String)}
   * anyway.
   *
   * @param conn the connection associated with the error
   * @param type the type of event that has occurred
   * @deprecated use {@link #connectionEvent(Connection, Events, Long, String)} instead
   */
  @Override
  @Deprecated
  public void connectionEvent(Connection conn, Events type) {}

  /**
   * Handles a NATS connection event and forwards it to the connection observer.
   *
   * @param conn the connection that raised the event
   * @param type the type of event
   * @param time the time of the event in milliseconds
   * @param uriDetails URI details of the connection
   */
  @Override
  public void connectionEvent(Connection conn, Events type, Long time, String uriDetails) {
    log.info("Noticing NATS connection event; type={}", type);
    natsConnectionObserver.onConnectionEvent(type);
  }

  /**
   * Handles a protocol error reported by NATS and forwards it to the error observer.
   *
   * @param conn the connection on which the error occurred
   * @param error the error description
   */
  @Override
  public void errorOccurred(Connection conn, String error) {
    log.error("An error occurred in NATS; error={}", error);
    natsErrorObserver.onError(error);
  }

  /**
   * Handles an exception from the NATS dispatch thread and forwards it to the error observer. Noise
   * exceptions during disconnect or reconnect are silently skipped.
   *
   * @param conn the connection on which the exception occurred
   * @param exp the exception
   */
  @Override
  public void exceptionOccurred(Connection conn, Exception exp) {
    // some exceptions are just noise (like getting EOF when listening on port while connection is
    // closed), skipping these
    if (shouldSkipLoggingException(conn, exp)) {
      return;
    }
    log.error("An exception occurred in NATS dispatch thread", exp);
    natsErrorObserver.onException(exp);
  }

  /**
   * Handles a slow consumer detection event and forwards it to the error observer.
   *
   * @param conn the connection that detected the slow consumer
   * @param consumer the slow consumer
   */
  @Override
  public void slowConsumerDetected(Connection conn, Consumer consumer) {
    log.warn("Detected NATS slow consumer");
    natsErrorObserver.onSlowConsumerDetected();
  }

  /**
   * Handles a discarded message event and forwards it to the error observer.
   *
   * @param conn the connection that discarded the message
   * @param msg the discarded message
   */
  @Override
  public void messageDiscarded(Connection conn, Message msg) {
    log.warn(
        "Message discarded on NATS subject; subject={}, metadata={}",
        msg.getSubject(),
        msg.metaData());
    natsErrorObserver.onMessageDiscarded();
  }

  private boolean shouldSkipLoggingException(Connection conn, Exception exp) {
    if ((exp instanceof IOException || exp instanceof ExecutionException)
        && (conn.getStatus() == Connection.Status.DISCONNECTED
            || conn.getStatus() == Connection.Status.RECONNECTING
            || conn.getStatus() == Connection.Status.CLOSED)) {
      return true;
    }
    if (exp instanceof IOException && "Read channel closed.".equals(exp.getMessage())) {
      return true;
    }
    return false;
  }
}
