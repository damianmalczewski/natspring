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

import io.github.malczuuu.natspring.core.NatsConnectionException;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.ConsumerContext;
import io.nats.client.Dispatcher;
import io.nats.client.ForceReconnectOptions;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.JetStreamOptions;
import io.nats.client.KeyValue;
import io.nats.client.KeyValueManagement;
import io.nats.client.KeyValueOptions;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.Nats;
import io.nats.client.ObjectStore;
import io.nats.client.ObjectStoreManagement;
import io.nats.client.ObjectStoreOptions;
import io.nats.client.Options;
import io.nats.client.Statistics;
import io.nats.client.StreamContext;
import io.nats.client.Subscription;
import io.nats.client.api.ServerInfo;
import io.nats.client.impl.Headers;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConnectionLifecycle} that manages the lifecycle of a NATS {@link Connection} and delegates
 * all {@link Connection} methods to the underlying managed connection.
 *
 * @since 0.1.0
 */
public class ManagedConnectionLifecycle implements ConnectionLifecycle {

  private static final Logger log = LoggerFactory.getLogger(ManagedConnectionLifecycle.class);

  private final Options options;

  private volatile @Nullable Connection delegate = null;

  /**
   * Creates a new {@link ManagedConnectionLifecycle}.
   *
   * @param options the NATS connection options
   */
  public ManagedConnectionLifecycle(Options options) {
    this.options = options;
  }

  /**
   * Establishes the NATS connection.
   *
   * @throws NatsConnectionException if the connection cannot be established
   */
  @Override
  public synchronized void start() {
    log.info("Establishing NATS connection at servers={}", options.getServers());
    try {
      delegate = Nats.connect(options);
    } catch (Exception e) {
      throw new NatsConnectionException("Failed to establish NATS connection", e);
    }
  }

  /**
   * Closes the NATS connection.
   *
   * @throws NatsConnectionException if the connection cannot be closed
   */
  @Override
  public synchronized void stop() {
    Connection delegate = this.delegate;
    if (delegate != null) {
      log.info("Closing NATS connection at servers={}", options.getServers());
      try {
        delegate.close();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new NatsConnectionException("Failed to close NATS connection", e);
      } catch (Exception e) {
        throw new NatsConnectionException("Failed to close NATS connection", e);
      } finally {
        this.delegate = null;
      }
    }
  }

  /**
   * Returns {@code true} if the connection has been established.
   *
   * @return {@code true} if running
   */
  @Override
  public boolean isRunning() {
    return delegate != null;
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to send the message to
   * @param body the message body
   * @throws IllegalStateException if the reconnect buffer is exceeded
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void publish(String subject, byte @Nullable [] body) {
    requireNonNull(delegate).publish(subject, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to send the message to
   * @param headers Optional headers to publish with the message.
   * @param body the message body
   * @throws IllegalStateException if the reconnect buffer is exceeded
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void publish(String subject, @Nullable Headers headers, byte @Nullable [] body) {
    requireNonNull(delegate).publish(subject, headers, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to send the message to
   * @param replyTo the subject the receiver should send any response to
   * @param body the message body
   * @throws IllegalStateException if the reconnect buffer is exceeded
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void publish(String subject, @Nullable String replyTo, byte @Nullable [] body) {
    requireNonNull(delegate).publish(subject, replyTo, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to send the message to
   * @param replyTo the subject the receiver should send any response to
   * @param headers Optional headers to publish with the message.
   * @param body the message body
   * @throws IllegalStateException if the reconnect buffer is exceeded
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void publish(
      String subject, @Nullable String replyTo, @Nullable Headers headers, byte @Nullable [] body) {
    requireNonNull(delegate).publish(subject, replyTo, headers, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param message the message
   * @throws IllegalStateException if the reconnect buffer is exceeded
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void publish(Message message) {
    requireNonNull(delegate).publish(message);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param body the content of the message
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> request(String subject, byte @Nullable [] body) {
    return requireNonNull(delegate).request(subject, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param headers Optional headers to publish with the message.
   * @param body the content of the message
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> request(
      String subject, @Nullable Headers headers, byte @Nullable [] body) {
    return requireNonNull(delegate).request(subject, headers, body);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param body the content of the message
   * @param timeout the time to wait for a response. If not supplied a default will be used.
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> requestWithTimeout(
      String subject, byte @Nullable [] body, @Nullable Duration timeout) {
    return requireNonNull(delegate).requestWithTimeout(subject, body, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param headers Optional headers to publish with the message.
   * @param body the content of the message
   * @param timeout the time to wait for a response
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> requestWithTimeout(
      String subject, @Nullable Headers headers, byte @Nullable [] body, Duration timeout) {
    return requireNonNull(delegate).requestWithTimeout(subject, headers, body, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param message the message
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> request(Message message) {
    return requireNonNull(delegate).request(message);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param message the message
   * @param timeout the time to wait for a response
   * @return a Future for the response, which may be cancelled on error or timed out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Message> requestWithTimeout(
      Message message, @Nullable Duration timeout) {
    return requireNonNull(delegate).requestWithTimeout(message, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param body the content of the message
   * @param timeout the time to wait for a response
   * @return the reply message or null if the timeout is reached
   * @throws InterruptedException if one is thrown while waiting, in order to propagate it up
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable Message request(
      String subject, byte @Nullable [] body, @Nullable Duration timeout)
      throws InterruptedException {
    return requireNonNull(delegate).request(subject, body, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject for the service that will handle the request
   * @param headers Optional headers to publish with the message.
   * @param body the content of the message
   * @param timeout the time to wait for a response
   * @return the reply message or null if the timeout is reached
   * @throws InterruptedException if one is thrown while waiting, in order to propagate it up
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable Message request(
      String subject, @Nullable Headers headers, byte @Nullable [] body, @Nullable Duration timeout)
      throws InterruptedException {
    return requireNonNull(delegate).request(subject, headers, body, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param message the message
   * @param timeout the time to wait for a response
   * @return the reply message or null if the timeout is reached
   * @throws InterruptedException if one is thrown while waiting, in order to propagate it up
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable Message request(Message message, @Nullable Duration timeout)
      throws InterruptedException {
    return requireNonNull(delegate).request(message, timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to subscribe to
   * @return an object representing the subscription
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Subscription subscribe(String subject) {
    return requireNonNull(delegate).subscribe(subject);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param subject the subject to subscribe to
   * @param queueName the queue group to join
   * @return an object representing the subscription
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Subscription subscribe(String subject, String queueName) {
    return requireNonNull(delegate).subscribe(subject, queueName);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param handler the target for the messages; if null, subscribing without a handler will discard
   *     messages
   * @return a new Dispatcher
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Dispatcher createDispatcher(@Nullable MessageHandler handler) {
    return requireNonNull(delegate).createDispatcher(handler);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return a new Dispatcher
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Dispatcher createDispatcher() {
    return requireNonNull(delegate).createDispatcher();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param dispatcher the dispatcher to close
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void closeDispatcher(Dispatcher dispatcher) {
    requireNonNull(delegate).closeDispatcher(dispatcher);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param connectionListener the ConnectionListener to attach; a null listener is a no-op
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void addConnectionListener(ConnectionListener connectionListener) {
    requireNonNull(delegate).addConnectionListener(connectionListener);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param connectionListener the ConnectionListener to detach
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void removeConnectionListener(ConnectionListener connectionListener) {
    requireNonNull(delegate).removeConnectionListener(connectionListener);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param timeout the time to wait for the flush to succeed; pass 0 or null to wait forever
   * @throws TimeoutException if the timeout is exceeded
   * @throws InterruptedException if the underlying thread is interrupted
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void flush(@Nullable Duration timeout) throws TimeoutException, InterruptedException {
    requireNonNull(delegate).flush(timeout);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param timeout the time to wait for the drain to succeed; pass 0 or null to wait forever
   * @return a Future that can be used to check if the drain has completed
   * @throws InterruptedException if the thread is interrupted
   * @throws TimeoutException if the initial flush times out
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public CompletableFuture<Boolean> drain(@Nullable Duration timeout)
      throws TimeoutException, InterruptedException {
    return requireNonNull(delegate).drain(timeout);
  }

  /**
   * Closes the NATS connection and releases all lifecycle resources.
   *
   * @throws NatsConnectionException if the connection cannot be closed
   */
  @Override
  public synchronized void close() throws InterruptedException {
    stop();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the connection's status
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Status getStatus() {
    return requireNonNull(delegate).getStatus();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the maximum size of a message payload
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public long getMaxPayload() {
    return requireNonNull(delegate).getMaxPayload();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return this connection's list of known server URLs
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Collection<String> getServers() {
    return requireNonNull(delegate).getServers();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the Statistics implementation
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Statistics getStatistics() {
    return requireNonNull(delegate).getStatistics();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the Options
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Options getOptions() {
    return requireNonNull(delegate).getOptions();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the server information such as id, client info, etc.
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ServerInfo getServerInfo() {
    return requireNonNull(delegate).getServerInfo();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the url string
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable String getConnectedUrl() {
    return requireNonNull(delegate).getConnectedUrl();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the InetAddress
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable InetAddress getClientInetAddress() {
    return requireNonNull(delegate).getClientInetAddress();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the last error text
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public @Nullable String getLastError() {
    return requireNonNull(delegate).getLastError();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void clearLastError() {
    requireNonNull(delegate).clearLastError();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the inbox
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public String createInbox() {
    return requireNonNull(delegate).createInbox();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @throws IOException if the connection flush fails
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void flushBuffer() throws IOException {
    requireNonNull(delegate).flushBuffer();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @throws IOException if the forceReconnect fails
   * @throws InterruptedException if the connection is not connected
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void forceReconnect() throws IOException, InterruptedException {
    requireNonNull(delegate).forceReconnect();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param options options for how the forceReconnect works
   * @throws IOException if the forceReconnect fails
   * @throws InterruptedException if the connection is not connected
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public void forceReconnect(@Nullable ForceReconnectOptions options)
      throws IOException, InterruptedException {
    requireNonNull(delegate).forceReconnect(options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the RTT as a duration
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public Duration RTT() throws IOException {
    return requireNonNull(delegate).RTT();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param streamName the stream for the context
   * @return a StreamContext instance
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws JetStreamApiException the request had an error related to the data
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public StreamContext getStreamContext(String streamName)
      throws IOException, JetStreamApiException {
    return requireNonNull(delegate).getStreamContext(streamName);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param streamName the stream for the context
   * @param options JetStream options; if null, default / no options are used
   * @return a StreamContext instance
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws JetStreamApiException the request had an error related to the data
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public StreamContext getStreamContext(String streamName, @Nullable JetStreamOptions options)
      throws IOException, JetStreamApiException {
    return requireNonNull(delegate).getStreamContext(streamName, options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param streamName the name of the stream
   * @param consumerName the name of the consumer
   * @return a ConsumerContext object
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws JetStreamApiException the request had an error related to the data
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ConsumerContext getConsumerContext(String streamName, String consumerName)
      throws IOException, JetStreamApiException {
    return requireNonNull(delegate).getConsumerContext(streamName, consumerName);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param streamName the name of the stream
   * @param consumerName the name of the consumer
   * @param options JetStream options; if null, default / no options are used
   * @return a ConsumerContext object
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws JetStreamApiException the request had an error related to the data
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ConsumerContext getConsumerContext(
      String streamName, String consumerName, @Nullable JetStreamOptions options)
      throws IOException, JetStreamApiException {
    return requireNonNull(delegate).getConsumerContext(streamName, consumerName, options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return a JetStream instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public JetStream jetStream() throws IOException {
    return requireNonNull(delegate).jetStream();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param options JetStream options; if null, default / no options are used
   * @return a JetStream instance
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public JetStream jetStream(@Nullable JetStreamOptions options) throws IOException {
    return requireNonNull(delegate).jetStream(options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return a JetStreamManagement instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public JetStreamManagement jetStreamManagement() throws IOException {
    return requireNonNull(delegate).jetStreamManagement();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param options JetStream options; if null, default / no options are used
   * @return a JetStreamManagement instance
   * @throws IOException covers various communication issues with the NATS server such as timeout or
   *     interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public JetStreamManagement jetStreamManagement(@Nullable JetStreamOptions options)
      throws IOException {
    return requireNonNull(delegate).jetStreamManagement(options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param bucketName the bucket name
   * @return a KeyValue instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public KeyValue keyValue(String bucketName) throws IOException {
    return requireNonNull(delegate).keyValue(bucketName);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param bucketName the bucket name
   * @param options KeyValue options; if null, default / no options are used
   * @return a KeyValue instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public KeyValue keyValue(String bucketName, @Nullable KeyValueOptions options)
      throws IOException {
    return requireNonNull(delegate).keyValue(bucketName, options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return a KeyValueManagement instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public KeyValueManagement keyValueManagement() throws IOException {
    return requireNonNull(delegate).keyValueManagement();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param options KeyValue options; if null, default / no options are used
   * @return a KeyValueManagement instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public KeyValueManagement keyValueManagement(@Nullable KeyValueOptions options)
      throws IOException {
    return requireNonNull(delegate).keyValueManagement(options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param bucketName the bucket name
   * @return an ObjectStore instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ObjectStore objectStore(String bucketName) throws IOException {
    return requireNonNull(delegate).objectStore(bucketName);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param bucketName the bucket name
   * @param options ObjectStore options; if null, default / no options are used
   * @return an ObjectStore instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ObjectStore objectStore(String bucketName, @Nullable ObjectStoreOptions options)
      throws IOException {
    return requireNonNull(delegate).objectStore(bucketName, options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return an ObjectStoreManagement instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ObjectStoreManagement objectStoreManagement() throws IOException {
    return requireNonNull(delegate).objectStoreManagement();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @param options ObjectStore options; if null, default / no options are used
   * @return an ObjectStoreManagement instance
   * @throws IOException various IO exception such as timeout or interruption
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public ObjectStoreManagement objectStoreManagement(ObjectStoreOptions options)
      throws IOException {
    return requireNonNull(delegate).objectStoreManagement(options);
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the number of messages in the outgoing queue
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public long outgoingPendingMessageCount() {
    return requireNonNull(delegate).outgoingPendingMessageCount();
  }

  /**
   * Delegates method call to the managed connection.
   *
   * @return the number of bytes in the outgoing queue
   * @throws NatsConnectionException if the connection is not available
   */
  @Override
  public long outgoingPendingBytes() {
    return requireNonNull(delegate).outgoingPendingBytes();
  }

  private Connection requireNonNull(@Nullable Connection delegate) {
    if (delegate == null) {
      throw new NatsConnectionException("Connection is not available");
    }
    return delegate;
  }
}
