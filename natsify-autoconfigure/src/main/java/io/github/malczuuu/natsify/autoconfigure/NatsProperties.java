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

/**
 * Configuration properties for Natsify Project, bound under the {@code natsify} prefix.
 *
 * @since 0.1.0
 */
@ConfigurationProperties(prefix = "natsify")
public class NatsProperties {

  /** Whether NATS auto-configuration is enabled. */
  private boolean enabled = true;

  /** NATS server URL. */
  private String server = "nats://localhost:4222";

  /** Username for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable String username = null;

  /** Password for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable String password = null;

  /** Optional name for the NATS connection. Used as the thread name in the client. */
  private @Nullable String connectionName = null;

  /** Maximum time to wait when establishing a connection. Uses the client default if not set. */
  private Duration connectionTimeout = Options.DEFAULT_CONNECTION_TIMEOUT;

  /** Maximum time to wait for a socket write to complete. Uses the client default if not set. */
  private Duration socketWriteTimeout = Options.DEFAULT_SOCKET_WRITE_TIMEOUT;

  /** Maximum number of reconnect attempts before giving up. {@code -1} means unlimited. */
  private int maxReconnects = Options.DEFAULT_MAX_RECONNECT;

  /** Time to wait between reconnect attempts. */
  private Duration reconnectWait = Options.DEFAULT_RECONNECT_WAIT;

  /** Random jitter added to {@code reconnectWait} for non-TLS connections. */
  private Duration reconnectJitter = Options.DEFAULT_RECONNECT_JITTER;

  /** Random jitter added to {@code reconnectWait} for TLS connections. */
  private Duration reconnectJitterTls = Options.DEFAULT_RECONNECT_JITTER_TLS;

  /** Size of the buffer (in bytes) used to hold published messages while reconnecting. */
  private long reconnectBufferSize = Options.DEFAULT_RECONNECT_BUF_SIZE;

  /** Interval between client-side pings to the server. */
  private Duration pingInterval = Options.DEFAULT_PING_INTERVAL;

  /**
   * Maximum number of pings that may be outstanding without a response before the connection is
   * considered stale.
   */
  private int maxPingsOut = Options.DEFAULT_MAX_PINGS_OUT;

  /** Interval at which the client scans for timed-out pending requests. */
  private Duration requestCleanupInterval = Options.DEFAULT_REQUEST_CLEANUP_INTERVAL;

  /** Prefix used for auto-generated inbox subjects. Must end with {@code .}. */
  private @Nullable String inboxPrefix = Options.DEFAULT_INBOX_PREFIX;

  /**
   * Whether the server should suppress echoing messages back to the connection that published them.
   */
  private boolean noEcho = false;

  /** Whether to disable randomization of the server list on connect and reconnect. */
  private boolean noRandomize = false;

  /**
   * Whether declared {@code StreamConfiguration} beans are used to create or update JetStream
   * streams on startup.
   */
  private boolean autoStreamCreation = false;

  /** Number of messages fetched per poll cycle for JetStream pull consumers. */
  private int pullFetchBatchSize = 200;

  /** Maximum time to wait for messages in each fetch call for JetStream pull consumers. */
  private Duration pullFetchTimeout = Duration.ofMillis(200);

  /**
   * Returns whether NATS auto-configuration is enabled.
   *
   * @return whether NATS auto-configuration is enabled
   * @since 0.1.0
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether NATS auto-configuration is enabled.
   *
   * @param enabled whether auto-configuration is enabled
   * @since 0.1.0
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the NATS server URL.
   *
   * @return the NATS server URL
   * @since 0.1.0
   */
  public String getServer() {
    return server;
  }

  /**
   * Sets the NATS server URL.
   *
   * @param server the NATS server URL
   * @since 0.1.0
   */
  public void setServer(String server) {
    this.server = server;
  }

  /**
   * Returns the username for authentication, or {@code null}.
   *
   * @return the username for authentication, or {@code null}
   * @since 0.1.0
   */
  public @Nullable String getUsername() {
    return username;
  }

  /**
   * Sets the username for authentication.
   *
   * @param username the username, or {@code null} to disable
   * @since 0.1.0
   */
  public void setUsername(@Nullable String username) {
    this.username = username;
  }

  /**
   * Returns the password for authentication, or {@code null}.
   *
   * @return the password for authentication, or {@code null}
   * @since 0.1.0
   */
  public @Nullable String getPassword() {
    return password;
  }

  /**
   * Sets the password for authentication.
   *
   * @param password the password, or {@code null} to disable
   * @since 0.1.0
   */
  public void setPassword(@Nullable String password) {
    this.password = password;
  }

  /**
   * Returns the name of the connection, or {@code null} if not specified. Used in thread name.
   *
   * @return the name of the connection, or {@code null}
   * @since 0.1.0
   */
  public @Nullable String getConnectionName() {
    return connectionName;
  }

  /**
   * Sets the name of the NATS connection.
   *
   * @param connectionName the connection name, or {@code null} for no name
   * @since 0.1.0
   */
  public void setConnectionName(@Nullable String connectionName) {
    this.connectionName = connectionName;
  }

  /**
   * Returns the maximum time to wait when establishing a connection.
   *
   * @return the connection timeout
   * @since 0.1.0
   */
  public Duration getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Sets the maximum time to wait when establishing a connection.
   *
   * @param connectionTimeout the connection timeout
   * @since 0.1.0
   */
  public void setConnectionTimeout(Duration connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Returns the maximum time to wait for a socket write to complete.
   *
   * @return the socket write timeout
   * @since 0.1.0
   */
  public Duration getSocketWriteTimeout() {
    return socketWriteTimeout;
  }

  /**
   * Sets the maximum time to wait for a socket write to complete.
   *
   * @param socketWriteTimeout the socket write timeout
   * @since 0.1.0
   */
  public void setSocketWriteTimeout(Duration socketWriteTimeout) {
    this.socketWriteTimeout = socketWriteTimeout;
  }

  /**
   * Returns the maximum number of reconnect attempts. {@code -1} means unlimited.
   *
   * @return the maximum reconnect count
   * @since 0.1.0
   */
  public int getMaxReconnects() {
    return maxReconnects;
  }

  /**
   * Sets the maximum number of reconnect attempts. Use {@code -1} for unlimited.
   *
   * @param maxReconnects the maximum reconnect count
   * @since 0.1.0
   */
  public void setMaxReconnects(int maxReconnects) {
    this.maxReconnects = maxReconnects;
  }

  /**
   * Returns the time to wait between reconnect attempts.
   *
   * @return the reconnect wait duration
   * @since 0.1.0
   */
  public Duration getReconnectWait() {
    return reconnectWait;
  }

  /**
   * Sets the time to wait between reconnect attempts.
   *
   * @param reconnectWait the reconnect wait duration
   * @since 0.1.0
   */
  public void setReconnectWait(Duration reconnectWait) {
    this.reconnectWait = reconnectWait;
  }

  /**
   * Returns the random jitter added to reconnect wait for non-TLS connections.
   *
   * @return the reconnect jitter
   * @since 0.1.0
   */
  public Duration getReconnectJitter() {
    return reconnectJitter;
  }

  /**
   * Sets the random jitter added to reconnect wait for non-TLS connections.
   *
   * @param reconnectJitter the reconnect jitter
   * @since 0.1.0
   */
  public void setReconnectJitter(Duration reconnectJitter) {
    this.reconnectJitter = reconnectJitter;
  }

  /**
   * Returns the random jitter added to reconnect wait for TLS connections.
   *
   * @return the TLS reconnect jitter
   * @since 0.1.0
   */
  public Duration getReconnectJitterTls() {
    return reconnectJitterTls;
  }

  /**
   * Sets the random jitter added to reconnect wait for TLS connections.
   *
   * @param reconnectJitterTls the TLS reconnect jitter
   * @since 0.1.0
   */
  public void setReconnectJitterTls(Duration reconnectJitterTls) {
    this.reconnectJitterTls = reconnectJitterTls;
  }

  /**
   * Returns the size in bytes of the buffer used to hold messages during reconnect.
   *
   * @return the reconnect buffer size
   * @since 0.1.0
   */
  public long getReconnectBufferSize() {
    return reconnectBufferSize;
  }

  /**
   * Sets the size in bytes of the buffer used to hold published messages while reconnecting.
   *
   * @param reconnectBufferSize the reconnect buffer size in bytes
   * @since 0.1.0
   */
  public void setReconnectBufferSize(long reconnectBufferSize) {
    this.reconnectBufferSize = reconnectBufferSize;
  }

  /**
   * Returns the interval between client-side pings to the server.
   *
   * @return the ping interval
   * @since 0.1.0
   */
  public Duration getPingInterval() {
    return pingInterval;
  }

  /**
   * Sets the interval between client-side pings to the server.
   *
   * @param pingInterval the ping interval
   * @since 0.1.0
   */
  public void setPingInterval(Duration pingInterval) {
    this.pingInterval = pingInterval;
  }

  /**
   * Returns the maximum number of outstanding pings before the connection is considered stale.
   *
   * @return the maximum pings out
   * @since 0.1.0
   */
  public int getMaxPingsOut() {
    return maxPingsOut;
  }

  /**
   * Sets the maximum number of outstanding pings before the connection is considered stale.
   *
   * @param maxPingsOut the maximum pings out
   * @since 0.1.0
   */
  public void setMaxPingsOut(int maxPingsOut) {
    this.maxPingsOut = maxPingsOut;
  }

  /**
   * Returns the interval for scanning timed-out pending requests.
   *
   * @return the request cleanup interval
   * @since 0.1.0
   */
  public Duration getRequestCleanupInterval() {
    return requestCleanupInterval;
  }

  /**
   * Sets the interval at which the client scans for timed-out pending requests.
   *
   * @param requestCleanupInterval the request cleanup interval
   * @since 0.1.0
   */
  public void setRequestCleanupInterval(Duration requestCleanupInterval) {
    this.requestCleanupInterval = requestCleanupInterval;
  }

  /**
   * Returns the prefix for auto-generated inbox subjects, or {@code null} to use the client default
   * ({@code _INBOX.}).
   *
   * @return the inbox prefix, or {@code null}
   * @since 0.1.0
   */
  public @Nullable String getInboxPrefix() {
    return inboxPrefix;
  }

  /**
   * Sets the prefix for auto-generated inbox subjects. Must end with {@code .}.
   *
   * @param inboxPrefix the inbox prefix, or {@code null} to use the client default
   * @since 0.1.0
   */
  public void setInboxPrefix(@Nullable String inboxPrefix) {
    this.inboxPrefix = inboxPrefix;
  }

  /**
   * Returns whether the server should suppress echoing published messages back to this connection.
   *
   * @return whether no-echo is enabled
   * @since 0.1.0
   */
  public boolean isNoEcho() {
    return noEcho;
  }

  /**
   * Sets whether the server should suppress echoing messages back to this connection.
   *
   * @param noEcho whether to enable no-echo
   * @since 0.1.0
   */
  public void setNoEcho(boolean noEcho) {
    this.noEcho = noEcho;
  }

  /**
   * Returns whether server list randomization is disabled.
   *
   * @return whether no-randomize is enabled
   * @since 0.1.0
   */
  public boolean isNoRandomize() {
    return noRandomize;
  }

  /**
   * Sets whether to disable server list randomization on connect and reconnect.
   *
   * @param noRandomize whether to disable randomization
   * @since 0.1.0
   */
  public void setNoRandomize(boolean noRandomize) {
    this.noRandomize = noRandomize;
  }

  /**
   * Returns whether JetStream stream auto-creation is enabled.
   *
   * @return whether JetStream stream auto-creation is enabled
   * @since 0.1.0
   */
  public boolean isAutoStreamCreation() {
    return autoStreamCreation;
  }

  /**
   * Sets whether declared {@code StreamConfiguration} beans are used to create or update JetStream
   * streams on startup.
   *
   * @param autoStreamCreation whether to enable auto stream creation
   * @since 0.1.0
   */
  public void setAutoStreamCreation(boolean autoStreamCreation) {
    this.autoStreamCreation = autoStreamCreation;
  }

  /**
   * Returns the number of messages to fetch per poll cycle for JetStream pull consumers.
   *
   * @return the pull fetch batch size
   * @since 0.1.0
   */
  public int getPullFetchBatchSize() {
    return pullFetchBatchSize;
  }

  /**
   * Sets the number of messages to fetch per poll cycle for JetStream pull consumers.
   *
   * @param pullFetchBatchSize the pull fetch batch size
   * @since 0.1.0
   */
  public void setPullFetchBatchSize(int pullFetchBatchSize) {
    this.pullFetchBatchSize = pullFetchBatchSize;
  }

  /**
   * Returns the maximum time to wait for messages in each fetch call for JetStream pull consumers.
   *
   * @return the pull fetch timeout
   * @since 0.1.0
   */
  public Duration getPullFetchTimeout() {
    return pullFetchTimeout;
  }

  /**
   * Sets the maximum time to wait for messages in each fetch call for JetStream pull consumers.
   *
   * @param pullFetchTimeout the pull fetch timeout
   * @since 0.1.0
   */
  public void setPullFetchTimeout(Duration pullFetchTimeout) {
    this.pullFetchTimeout = pullFetchTimeout;
  }
}
