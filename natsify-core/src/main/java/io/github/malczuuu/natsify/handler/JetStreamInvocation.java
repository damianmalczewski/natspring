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

package io.github.malczuuu.natsify.handler;

import io.github.malczuuu.natsify.annotation.AckMode;
import io.github.malczuuu.natsify.instrument.JetStreamListenerObserver;
import io.nats.client.Message;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class JetStreamInvocation implements Consumer<Message> {

  private static final Logger log = LoggerFactory.getLogger(JetStreamInvocation.class);

  private final JetStreamListenerDetails listener;
  private final MessageArgumentResolver argumentResolver;
  private final JetStreamListenerObserver observer;

  JetStreamInvocation(
      JetStreamListenerDetails listener,
      MessageArgumentResolver argumentResolver,
      JetStreamListenerObserver observer) {
    this.listener = listener;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
  }

  @Override
  public void accept(Message msg) {
    observer.onReceived(listener.getSubject(), listener.getStream());
    long start = System.nanoTime();
    try {
      doAccept(msg);
    } finally {
      observer.onProcessed(listener.getSubject(), listener.getStream(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message msg) {
    Object[] args;
    try {
      args = argumentResolver.resolveArguments(listener.getMethod().getParameters(), msg);
    } catch (Exception e) {
      logMessageException(msg, e);
      msg.term();
      observer.onTerminated(listener.getSubject(), listener.getStream(), e);
      return;
    }

    try {
      listener.getMethod().invoke(listener.getBean(), args);
      if (listener.getAckMode() == AckMode.AUTO) {
        msg.ack();
        observer.onAcked(listener.getSubject(), listener.getStream());
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.error("Failed to invoke handler for NATS JetStream listener {}", listener.getMethod(), e);
      if (listener.getAckMode() == AckMode.AUTO) {
        msg.nak();
        observer.onNacked(listener.getSubject(), listener.getStream());
      }
    }
  }

  private void logMessageException(Message msg, Exception e) {
    String stream = null;
    Long streamSequence = null;
    Long consumerSequence = null;
    Long deliveredCount = null;
    Instant timestamp = null;

    if (msg.metaData() != null) {
      stream = msg.metaData().getStream();
      streamSequence = msg.metaData().streamSequence();
      consumerSequence = msg.metaData().consumerSequence();
      deliveredCount = msg.metaData().deliveredCount();
      timestamp = msg.metaData().timestamp().toInstant();
    }

    log.error(
        "Unable to resolve arguments for NATS JetStream handler {}.{}, terminating message, subject={}, stream={}, streamSequence={}, consumerSequence={}, deliveredCount={}, timestamp={}",
        AopUtils.getTargetClass(listener.getBean()).getSimpleName(),
        listener.getMethod().getName(),
        msg.getSubject(),
        stream,
        streamSequence,
        consumerSequence,
        deliveredCount,
        timestamp,
        e);
  }
}
