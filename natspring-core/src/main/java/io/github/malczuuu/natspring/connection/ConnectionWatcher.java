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

import io.github.malczuuu.natspring.instrument.NatsConnectionObserver;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges NATS {@link ConnectionListener} and {@link ErrorListener} events to a {@link
 * NatsConnectionObserver}.
 *
 * @since 0.1.0
 */
public class ConnectionWatcher implements ConnectionListener, ErrorListener {

  private static final Logger log = LoggerFactory.getLogger(ConnectionWatcher.class);

  private final NatsConnectionObserver connectionObserver;

  /**
   * Creates a new {@link ConnectionWatcher}.
   *
   * @param connectionObserver the observer to notify on connection and error events
   */
  public ConnectionWatcher(NatsConnectionObserver connectionObserver) {
    this.connectionObserver = connectionObserver;
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
    connectionObserver.onConnectionEvent(type);
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
    connectionObserver.onError(error);
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
    log.error("An exception occurred in NATS dispatch thread", exp);
    connectionObserver.onException(exp);
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
    connectionObserver.onSlowConsumerDetected();
  }

  /**
   * Handles a discarded message event and forwards it to the error observer.
   *
   * @param conn the connection that discarded the message
   * @param message the discarded message
   */
  @Override
  public void messageDiscarded(Connection conn, Message message) {
    log.warn(
        "Message discarded on NATS subject; subject={}, metadata={}",
        message.getSubject(),
        message.metaData());
    connectionObserver.onMessageDiscarded();
  }
}
