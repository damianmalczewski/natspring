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

import io.github.malczuuu.natsify.handler.JetStreamListenerManager;
import io.github.malczuuu.natsify.handler.JetStreamListenerRegistry;
import io.github.malczuuu.natsify.handler.MessageArgumentResolver;
import io.github.malczuuu.natsify.handler.NatsListenerManager;
import io.github.malczuuu.natsify.handler.NatsListenerRegistry;
import io.github.malczuuu.natsify.handler.SimpleMessageArgumentResolver;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.github.malczuuu.natsify.instrument.NatsConnectionObserver;
import io.github.malczuuu.natsify.instrument.NatsErrorObserver;
import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

public class ConnectionConfigurer implements ConnectionManager, ConnectionListener, ErrorListener {

  private static final Logger log = LoggerFactory.getLogger(ConnectionConfigurer.class);

  private final List<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizers;
  private final NatsListenerManager natsListenerManager;
  private final JetStreamListenerManager jetStreamListenerManager;
  private final NatsConnectionObserver natsConnectionObserver;
  private final NatsErrorObserver natsErrorObserver;

  private Options options = Options.builder().build();
  private @Nullable Connection connection = null;

  public ConnectionConfigurer(
      List<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizers,
      NatsListenerRegistry handlerRegistry,
      JetStreamListenerRegistry jetStreamRegistry,
      JsonMapper jsonMapper,
      NatsListenerObserver natsListenerObserver,
      JetStreamListenerObserver jetStreamListenerObserver,
      NatsConnectionObserver natsConnectionObserver,
      NatsErrorObserver natsErrorObserver) {
    this.connectionOptionsBuilderCustomizers = connectionOptionsBuilderCustomizers;
    MessageArgumentResolver argumentResolver = new SimpleMessageArgumentResolver(jsonMapper);
    this.natsListenerManager =
        new NatsListenerManager(handlerRegistry, argumentResolver, natsListenerObserver);
    this.jetStreamListenerManager =
        new JetStreamListenerManager(
            jetStreamRegistry, argumentResolver, jetStreamListenerObserver);
    this.natsConnectionObserver = natsConnectionObserver;
    this.natsErrorObserver = natsErrorObserver;
  }

  @Override
  public Connection getConnection() {
    Connection connection = this.connection;
    if (connection == null) {
      throw new IllegalStateException("NATS connection not available");
    }
    return connection;
  }

  @Override
  public synchronized void start() {
    Options.Builder builder = Options.builder();
    for (ConnectionOptionsBuilderCustomizer customizer : connectionOptionsBuilderCustomizers) {
      builder = customizer.customize(builder);
    }

    builder = builder.connectionListener(this).errorListener(this);
    options = builder.build();

    log.info("Establishing NATS connection at {}", options.getServers());
    try {
      Connection connection = Nats.connect(builder.build());
      this.connection = connection;

      natsListenerManager.initialize(connection);
      jetStreamListenerManager.initialize(connection);
    } catch (Exception e) {
      throw new RuntimeException("Failed to establish NATS connection", e);
    }
  }

  @Override
  public synchronized void stop() {
    log.info("Closing NATS connection at {}", options.getServers());

    jetStreamListenerManager.stop();
    natsListenerManager.stop();

    try {
      Connection connection = this.connection;
      if (connection != null) {
        connection.close();
      }
      this.connection = null;
    } catch (Exception e) {
      throw new RuntimeException("Failed to drain NATS connection", e);
    }
  }

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

  @Override
  public void connectionEvent(Connection conn, Events type, Long time, String uriDetails) {
    log.info("Noticing NATS connection event; type={}", type);
    natsConnectionObserver.onConnectionEvent(type);
  }

  @Override
  public void errorOccurred(Connection conn, String error) {
    log.error("An error occurred in NATS; error={}", error);
    natsErrorObserver.onError(error);
  }

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

  @Override
  public void slowConsumerDetected(Connection conn, Consumer consumer) {
    log.warn("Detected NATS slow consumer");
    natsErrorObserver.onSlowConsumerDetected();
  }

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
