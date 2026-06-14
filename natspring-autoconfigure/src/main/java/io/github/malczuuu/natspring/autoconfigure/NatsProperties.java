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

package io.github.malczuuu.natspring.autoconfigure;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Natspring Project, bound under the {@code nats} prefix.
 *
 * @since 0.1.0
 */
@ConfigurationProperties(prefix = "nats")
public class NatsProperties {

  /** Whether NATS auto-configuration is enabled. */
  private boolean enabled = true;

  /** NATS server URL. */
  private String server = "nats://localhost:4222";

  /** Username for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable String username = null;

  /** Password for NATS authentication. Omit if the server requires no credentials. */
  private @Nullable String password = null;

  /**
   * Whether declared {@code StreamConfiguration} beans are used to create JetStream streams on
   * startup.
   */
  private boolean autoStreamCreation = false;

  /** Number of messages fetched per poll cycle for JetStream pull consumers. */
  private int pullFetchBatchSize = 200;

  /** Maximum time to wait for messages in each fetch call for JetStream pull consumers. */
  private Duration pullFetchTimeout = Duration.ofMillis(200);

  /** Optional name for the NATS connection. Used as the thread name in the client. */
  private @Nullable String connectionName = null;

  /** Maximum time to wait when establishing a connection. */
  private @Nullable Duration connectionTimeout = null;

  /** Maximum time to wait for a socket write to complete. */
  private @Nullable Duration socketWriteTimeout = null;

  /** Maximum number of reconnect attempts before giving up. {@code -1} means unlimited. */
  private @Nullable Integer maxReconnects = null;

  /** Time to wait between reconnect attempts. */
  private @Nullable Duration reconnectWait = null;

  /** Random jitter added to {@code reconnectWait} for non-TLS connections. */
  private @Nullable Duration reconnectJitter = null;

  /** Random jitter added to {@code reconnectWait} for TLS connections. */
  private @Nullable Duration reconnectJitterTls = null;

  /** Size of the buffer (in bytes) used to hold published messages while reconnecting. */
  private @Nullable Long reconnectBufferSize = null;

  /** Interval between client-side pings to the server. */
  private @Nullable Duration pingInterval = null;

  /**
   * Maximum number of pings that may be outstanding without a response before the connection is
   * considered stale.
   */
  private @Nullable Integer maxPingsOut = null;

  /** Interval at which the client scans for timed-out pending requests. */
  private @Nullable Duration requestCleanupInterval = null;

  /** Prefix used for auto-generated inbox subjects. Must end with {@code .}. */
  private @Nullable String inboxPrefix = null;

  /**
   * Whether the server should suppress echoing messages back to the connection that published them.
   */
  private boolean noEcho = false;

  /** Whether to disable randomization of the server list on connect and reconnect. */
  private boolean noRandomize = false;

  /** Creates a new {@link NatsProperties}. */
  public NatsProperties() {}

  /**
   * Returns whether NATS auto-configuration is enabled.
   *
   * @return whether NATS auto-configuration is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether NATS auto-configuration is enabled.
   *
   * @param enabled whether auto-configuration is enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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
   * Sets the NATS server URL.
   *
   * @param server the NATS server URL
   */
  public void setServer(String server) {
    this.server = server;
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
   * Sets the username for authentication.
   *
   * @param username the username, or {@code null} to disable
   */
  public void setUsername(@Nullable String username) {
    this.username = username;
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
   * Sets the password for authentication.
   *
   * @param password the password, or {@code null} to disable
   */
  public void setPassword(@Nullable String password) {
    this.password = password;
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
   * Sets whether declared {@code StreamConfiguration} beans are used to create JetStream streams on
   * startup.
   *
   * @param autoStreamCreation whether to enable auto stream creation
   */
  public void setAutoStreamCreation(boolean autoStreamCreation) {
    this.autoStreamCreation = autoStreamCreation;
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
   * Sets the number of messages to fetch per poll cycle for JetStream pull consumers.
   *
   * @param pullFetchBatchSize the pull fetch batch size
   */
  public void setPullFetchBatchSize(int pullFetchBatchSize) {
    this.pullFetchBatchSize = pullFetchBatchSize;
  }

  /**
   * Returns the maximum time to wait for messages in each fetch call for JetStream pull consumers.
   *
   * @return the pull fetch timeout
   */
  public Duration getPullFetchTimeout() {
    return pullFetchTimeout;
  }

  /**
   * Sets the maximum time to wait for messages in each fetch call for JetStream pull consumers.
   *
   * @param pullFetchTimeout the pull fetch timeout
   */
  public void setPullFetchTimeout(Duration pullFetchTimeout) {
    this.pullFetchTimeout = pullFetchTimeout;
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
   * Sets the name of the NATS connection.
   *
   * @param connectionName the connection name, or {@code null} for no name
   */
  public void setConnectionName(@Nullable String connectionName) {
    this.connectionName = connectionName;
  }

  /**
   * Returns the maximum time to wait when establishing a connection, or {@code null} to use the
   * NATS Java client default.
   *
   * @return the connection timeout, or {@code null}
   */
  public @Nullable Duration getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Sets the maximum time to wait when establishing a connection.
   *
   * @param connectionTimeout the connection timeout, or {@code null} to use the NATS Java client
   *     default
   */
  public void setConnectionTimeout(@Nullable Duration connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Returns the maximum time to wait for a socket write to complete, or {@code null} to use the
   * NATS Java client default.
   *
   * @return the socket write timeout, or {@code null}
   */
  public @Nullable Duration getSocketWriteTimeout() {
    return socketWriteTimeout;
  }

  /**
   * Sets the maximum time to wait for a socket write to complete.
   *
   * @param socketWriteTimeout the socket write timeout, or {@code null} to use the NATS Java client
   *     default
   */
  public void setSocketWriteTimeout(@Nullable Duration socketWriteTimeout) {
    this.socketWriteTimeout = socketWriteTimeout;
  }

  /**
   * Returns the maximum number of reconnect attempts, or {@code null} to use the NATS Java client
   * default. {@code -1} means unlimited.
   *
   * @return the maximum reconnect count, or {@code null}
   */
  public @Nullable Integer getMaxReconnects() {
    return maxReconnects;
  }

  /**
   * Sets the maximum number of reconnect attempts. Use {@code -1} for unlimited.
   *
   * @param maxReconnects the maximum reconnect count, or {@code null} to use the NATS Java client
   *     default
   */
  public void setMaxReconnects(@Nullable Integer maxReconnects) {
    this.maxReconnects = maxReconnects;
  }

  /**
   * Returns the time to wait between reconnect attempts, or {@code null} to use the NATS Java
   * client default.
   *
   * @return the reconnect wait duration, or {@code null}
   */
  public @Nullable Duration getReconnectWait() {
    return reconnectWait;
  }

  /**
   * Sets the time to wait between reconnect attempts.
   *
   * @param reconnectWait the reconnect wait duration, or {@code null} to use the NATS Java client
   *     default
   */
  public void setReconnectWait(@Nullable Duration reconnectWait) {
    this.reconnectWait = reconnectWait;
  }

  /**
   * Returns the random jitter added to reconnect wait for non-TLS connections, or {@code null} to
   * use the NATS Java client default.
   *
   * @return the reconnect jitter, or {@code null}
   */
  public @Nullable Duration getReconnectJitter() {
    return reconnectJitter;
  }

  /**
   * Sets the random jitter added to reconnect wait for non-TLS connections.
   *
   * @param reconnectJitter the reconnect jitter, or {@code null} to use the NATS Java client
   *     default
   */
  public void setReconnectJitter(@Nullable Duration reconnectJitter) {
    this.reconnectJitter = reconnectJitter;
  }

  /**
   * Returns the random jitter added to reconnect wait for TLS connections, or {@code null} to use
   * the NATS Java client default.
   *
   * @return the TLS reconnect jitter, or {@code null}
   */
  public @Nullable Duration getReconnectJitterTls() {
    return reconnectJitterTls;
  }

  /**
   * Sets the random jitter added to reconnect wait for TLS connections.
   *
   * @param reconnectJitterTls the TLS reconnect jitter, or {@code null} to use the NATS Java client
   *     default
   */
  public void setReconnectJitterTls(@Nullable Duration reconnectJitterTls) {
    this.reconnectJitterTls = reconnectJitterTls;
  }

  /**
   * Returns the size in bytes of the buffer used to hold messages during reconnect, or {@code null}
   * to use the NATS Java client default.
   *
   * @return the reconnect buffer size, or {@code null}
   */
  public @Nullable Long getReconnectBufferSize() {
    return reconnectBufferSize;
  }

  /**
   * Sets the size in bytes of the buffer used to hold published messages while reconnecting.
   *
   * @param reconnectBufferSize the reconnect buffer size in bytes, or {@code null} to use the NATS
   *     Java client default
   */
  public void setReconnectBufferSize(@Nullable Long reconnectBufferSize) {
    this.reconnectBufferSize = reconnectBufferSize;
  }

  /**
   * Returns the interval between client-side pings to the server, or {@code null} to use the NATS
   * Java client default.
   *
   * @return the ping interval, or {@code null}
   */
  public @Nullable Duration getPingInterval() {
    return pingInterval;
  }

  /**
   * Sets the interval between client-side pings to the server.
   *
   * @param pingInterval the ping interval, or {@code null} to use the NATS Java client default
   */
  public void setPingInterval(@Nullable Duration pingInterval) {
    this.pingInterval = pingInterval;
  }

  /**
   * Returns the maximum number of outstanding pings before the connection is considered stale, or
   * {@code null} to use the NATS Java client default.
   *
   * @return the maximum pings out, or {@code null}
   */
  public @Nullable Integer getMaxPingsOut() {
    return maxPingsOut;
  }

  /**
   * Sets the maximum number of outstanding pings before the connection is considered stale.
   *
   * @param maxPingsOut the maximum pings out, or {@code null} to use the NATS Java client default
   */
  public void setMaxPingsOut(@Nullable Integer maxPingsOut) {
    this.maxPingsOut = maxPingsOut;
  }

  /**
   * Returns the interval for scanning timed-out pending requests, or {@code null} to use the NATS
   * Java client default.
   *
   * @return the request cleanup interval, or {@code null}
   */
  public @Nullable Duration getRequestCleanupInterval() {
    return requestCleanupInterval;
  }

  /**
   * Sets the interval at which the client scans for timed-out pending requests.
   *
   * @param requestCleanupInterval the request cleanup interval, or {@code null} to use the NATS
   *     Java client default
   */
  public void setRequestCleanupInterval(@Nullable Duration requestCleanupInterval) {
    this.requestCleanupInterval = requestCleanupInterval;
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
   * Sets the prefix for auto-generated inbox subjects. Must end with {@code .}.
   *
   * @param inboxPrefix the inbox prefix, or {@code null} to use the client default
   */
  public void setInboxPrefix(@Nullable String inboxPrefix) {
    this.inboxPrefix = inboxPrefix;
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
   * Sets whether the server should suppress echoing messages back to this connection.
   *
   * @param noEcho whether to enable no-echo
   */
  public void setNoEcho(boolean noEcho) {
    this.noEcho = noEcho;
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
   * Sets whether to disable server list randomization on connect and reconnect.
   *
   * @param noRandomize whether to disable randomization
   */
  public void setNoRandomize(boolean noRandomize) {
    this.noRandomize = noRandomize;
  }
}
