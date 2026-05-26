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

package io.github.malczuuu.natsify.autoconfigure;

import io.nats.client.Options;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration properties for Natsify Project, bound under the {@code natsify} prefix. */
@ConfigurationProperties(prefix = "natsify")
public class NatsProperties {

  /** Whether NATS auto-configuration is enabled. Default: {@code true}. */
  private final boolean enabled;

  /** NATS server URL. Default: {@code nats://localhost:4222}. */
  private final String server;

  /** Username for NATS authentication. Omit if the server requires no credentials. */
  private final @Nullable String username;

  /** Password for NATS authentication. Omit if the server requires no credentials. */
  private final @Nullable String password;

  /** Optional name for the NATS connection. Used as the thread name in the client. */
  private final @Nullable String connectionName;

  /**
   * Maximum time to wait when establishing a connection. Uses the client default if {@code null}.
   */
  private final Duration connectionTimeout;

  /**
   * Maximum time to wait for a socket write to complete. Uses the client default if {@code null}.
   */
  private final Duration socketWriteTimeout;

  /**
   * Maximum number of reconnect attempts before giving up. {@code -1} means unlimited. Default:
   * {@code 60}.
   */
  private final int maxReconnects;

  /** Time to wait between reconnect attempts. Default: {@code 2s}. */
  private final Duration reconnectWait;

  /**
   * Random jitter added to {@code reconnectWait} for non-TLS connections. Default: {@code 100ms}.
   */
  private final Duration reconnectJitter;

  /** Random jitter added to {@code reconnectWait} for TLS connections. Default: {@code 1s}. */
  private final Duration reconnectJitterTls;

  /**
   * Size of the buffer (in bytes) used to hold published messages while reconnecting. Default:
   * {@code 8388608} (8 MB).
   */
  private final long reconnectBufferSize;

  /** Interval between client-side pings to the server. Default: {@code 2m}. */
  private final Duration pingInterval;

  /**
   * Maximum number of pings that may be outstanding without a response before the connection is
   * considered stale. Default: {@code 2}.
   */
  private final int maxPingsOut;

  /** Interval at which the client scans for timed-out pending requests. Default: {@code 5s}. */
  private final Duration requestCleanupInterval;

  /**
   * Prefix used for auto-generated inbox subjects. Must end with {@code .}. Default: {@code
   * _INBOX.}.
   */
  private final @Nullable String inboxPrefix;

  /**
   * Whether the server should suppress echoing messages back to the connection that published them.
   * Default: {@code false}.
   */
  private final boolean noEcho;

  /**
   * Whether to disable randomization of the server list on connect and reconnect. Default: {@code
   * false}.
   */
  private final boolean noRandomize;

  /**
   * Whether declared {@code StreamConfiguration} beans are used to create or update JetStream
   * streams on startup. Default: {@code false}.
   */
  private final boolean autoStreamCreation;

  /**
   * Number of messages fetched per poll cycle for JetStream pull consumers. Default: {@code 200}.
   */
  private final int pullFetchBatchSize;

  /**
   * Maximum time to wait for messages in each fetch call for JetStream pull consumers. Default:
   * {@code 200ms}.
   */
  private final Duration pullFetchTimeout;

  /**
   * Creates a new {@code NatsProperties} instance. Intended for use by the Spring Boot
   * configuration binding mechanism; prefer injecting the bound bean over constructing directly.
   *
   * @param enabled whether auto-configuration is enabled
   * @param server the NATS server URL
   * @param username optional username for authentication
   * @param password optional password for authentication
   * @param connectionName optional name for the connection, used as thread name
   * @param connectionTimeout optional maximum time to wait when establishing a connection
   * @param socketWriteTimeout optional maximum time to wait for a socket write to complete
   * @param maxReconnects maximum number of reconnect attempts; {@code -1} for unlimited
   * @param reconnectWait time to wait between reconnect attempts
   * @param reconnectJitter random jitter added to reconnect wait for non-TLS connections
   * @param reconnectJitterTls random jitter added to reconnect wait for TLS connections
   * @param reconnectBufferSize size in bytes of the buffer used during reconnect
   * @param pingInterval interval between client-side pings to the server
   * @param maxPingsOut maximum outstanding pings before the connection is considered stale
   * @param requestCleanupInterval interval for scanning timed-out pending requests
   * @param inboxPrefix prefix for auto-generated inbox subjects; must end with {@code .}
   * @param noEcho whether the server should suppress echoing back published messages
   * @param noRandomize whether to disable server list randomization
   * @param autoStreamCreation whether JetStream streams should be created or updated on startup
   * @param pullFetchBatchSize number of messages to fetch per poll cycle for pull consumers
   * @param pullFetchTimeout maximum time to wait for messages in each fetch call for pull consumers
   */
  public NatsProperties(
      @DefaultValue("true") boolean enabled,
      @DefaultValue("nats://localhost:4222") String server,
      @Nullable String username,
      @Nullable String password,
      @Nullable String connectionName,
      @Nullable Duration connectionTimeout,
      @Nullable Duration socketWriteTimeout,
      @DefaultValue("60") int maxReconnects,
      @Nullable Duration reconnectWait,
      @Nullable Duration reconnectJitter,
      @Nullable Duration reconnectJitterTls,
      @DefaultValue("8388608") long reconnectBufferSize,
      @Nullable Duration pingInterval,
      @DefaultValue("2") int maxPingsOut,
      @Nullable Duration requestCleanupInterval,
      @Nullable String inboxPrefix,
      @DefaultValue("false") boolean noEcho,
      @DefaultValue("false") boolean noRandomize,
      @DefaultValue("false") boolean autoStreamCreation,
      @DefaultValue("200") int pullFetchBatchSize,
      @DefaultValue("200ms") Duration pullFetchTimeout) {
    this.enabled = enabled;
    this.server = server;
    this.username = username;
    this.password = password;
    this.connectionName = connectionName;
    this.connectionTimeout =
        connectionTimeout != null ? connectionTimeout : Options.DEFAULT_CONNECTION_TIMEOUT;
    this.socketWriteTimeout =
        socketWriteTimeout != null ? socketWriteTimeout : Options.DEFAULT_SOCKET_WRITE_TIMEOUT;
    this.maxReconnects = maxReconnects;
    this.reconnectWait = reconnectWait != null ? reconnectWait : Options.DEFAULT_RECONNECT_WAIT;
    this.reconnectJitter =
        reconnectJitter != null ? reconnectJitter : Options.DEFAULT_RECONNECT_JITTER;
    this.reconnectJitterTls =
        reconnectJitterTls != null ? reconnectJitterTls : Options.DEFAULT_RECONNECT_JITTER_TLS;
    this.reconnectBufferSize = reconnectBufferSize;
    this.pingInterval = pingInterval != null ? pingInterval : Options.DEFAULT_PING_INTERVAL;
    this.maxPingsOut = maxPingsOut;
    this.requestCleanupInterval =
        requestCleanupInterval != null
            ? requestCleanupInterval
            : Options.DEFAULT_REQUEST_CLEANUP_INTERVAL;
    this.inboxPrefix = inboxPrefix;
    this.noEcho = noEcho;
    this.noRandomize = noRandomize;
    this.autoStreamCreation = autoStreamCreation;
    this.pullFetchBatchSize = pullFetchBatchSize;
    this.pullFetchTimeout = pullFetchTimeout;
  }

  /**
   * Returns whether NATS auto-configuration is enabled.
   *
   * @return whether NATS auto-configuration is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns the NATS server URL.
   *
   * @return the NATS server URL
   */
  public String getServer() {
    return server;
  }

  /**
   * Returns the username for authentication, or {@code null}.
   *
   * @return the username for authentication, or {@code null}
   */
  public @Nullable String getUsername() {
    return username;
  }

  /**
   * Returns the password for authentication, or {@code null}.
   *
   * @return the password for authentication, or {@code null}
   */
  public @Nullable String getPassword() {
    return password;
  }

  /**
   * Returns the name of the connection, or {@code null} if not specified. Used in thread name.
   *
   * @return the name of the connection, or {@code null}
   */
  public @Nullable String getConnectionName() {
    return connectionName;
  }

  /**
   * Returns the maximum time to wait when establishing a connection.
   *
   * @return the connection timeout
   */
  public Duration getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Returns the maximum time to wait for a socket write to complete.
   *
   * @return the socket write timeout
   */
  public Duration getSocketWriteTimeout() {
    return socketWriteTimeout;
  }

  /**
   * Returns the maximum number of reconnect attempts. {@code -1} means unlimited.
   *
   * @return the maximum reconnect count
   */
  public int getMaxReconnects() {
    return maxReconnects;
  }

  /**
   * Returns the time to wait between reconnect attempts.
   *
   * @return the reconnect wait duration
   */
  public Duration getReconnectWait() {
    return reconnectWait;
  }

  /**
   * Returns the random jitter added to reconnect wait for non-TLS connections.
   *
   * @return the reconnect jitter
   */
  public Duration getReconnectJitter() {
    return reconnectJitter;
  }

  /**
   * Returns the random jitter added to reconnect wait for TLS connections.
   *
   * @return the TLS reconnect jitter
   */
  public Duration getReconnectJitterTls() {
    return reconnectJitterTls;
  }

  /**
   * Returns the size in bytes of the buffer used to hold messages during reconnect.
   *
   * @return the reconnect buffer size
   */
  public long getReconnectBufferSize() {
    return reconnectBufferSize;
  }

  /**
   * Returns the interval between client-side pings to the server.
   *
   * @return the ping interval
   */
  public Duration getPingInterval() {
    return pingInterval;
  }

  /**
   * Returns the maximum number of outstanding pings before the connection is considered stale.
   *
   * @return the maximum pings out
   */
  public int getMaxPingsOut() {
    return maxPingsOut;
  }

  /**
   * Returns the interval for scanning timed-out pending requests.
   *
   * @return the request cleanup interval
   */
  public Duration getRequestCleanupInterval() {
    return requestCleanupInterval;
  }

  /**
   * Returns the prefix for auto-generated inbox subjects, or {@code null} to use the client default
   * ({@code _INBOX.}).
   *
   * @return the inbox prefix, or {@code null}
   */
  public @Nullable String getInboxPrefix() {
    return inboxPrefix;
  }

  /**
   * Returns whether the server should suppress echoing published messages back to this connection.
   *
   * @return whether no-echo is enabled
   */
  public boolean isNoEcho() {
    return noEcho;
  }

  /**
   * Returns whether server list randomization is disabled.
   *
   * @return whether no-randomize is enabled
   */
  public boolean isNoRandomize() {
    return noRandomize;
  }

  /**
   * Returns whether JetStream stream auto-creation is enabled.
   *
   * @return whether JetStream stream auto-creation is enabled
   */
  public boolean isAutoStreamCreation() {
    return autoStreamCreation;
  }

  /**
   * Returns the number of messages to fetch per poll cycle for JetStream pull consumers.
   *
   * @return the pull fetch batch size
   */
  public int getPullFetchBatchSize() {
    return pullFetchBatchSize;
  }

  /**
   * Returns the maximum time to wait for messages in each fetch call for JetStream pull consumers.
   *
   * @return the pull fetch timeout
   */
  public Duration getPullFetchTimeout() {
    return pullFetchTimeout;
  }
}
