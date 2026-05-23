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
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JetStreamInvocation implements Consumer<Message> {

  private static final Logger log = LoggerFactory.getLogger(JetStreamInvocation.class);

  private final JetStreamListenerHandle handle;
  private final MessageArgumentResolver argumentResolver;
  private final JetStreamListenerObserver observer;

  JetStreamInvocation(
      JetStreamListenerHandle handle,
      MessageArgumentResolver argumentResolver,
      JetStreamListenerObserver observer) {
    this.handle = handle;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
  }

  @Override
  public void accept(Message msg) {
    observer.onReceived(handle.getSubject(), handle.getStream());
    long start = System.nanoTime();
    try {
      doAccept(msg);
    } finally {
      observer.onProcessed(handle.getSubject(), handle.getStream(), System.nanoTime() - start);
    }
  }

  private void doAccept(Message msg) {
    Object[] args;
    try {
      args = argumentResolver.resolveArguments(handle.getMethod().getParameters(), msg);
    } catch (Exception e) {
      log.error(
          "Unable to resolve arguments for NATS JetStream handler {}, terminating message",
          handle.getMethod(),
          e);
      msg.term();
      observer.onTerminated(handle.getSubject(), handle.getStream(), e);
      return;
    }

    try {
      handle.getMethod().invoke(handle.getBean(), args);
      if (handle.getAckMode() == AckMode.AUTO) {
        msg.ack();
        observer.onAcked(handle.getSubject(), handle.getStream());
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      log.error("Failed to invoke handler for NATS JetStream listener {}", handle.getMethod(), e);
      if (handle.getAckMode() == AckMode.AUTO) {
        msg.nak();
        observer.onNacked(handle.getSubject(), handle.getStream());
      }
    }
  }
}
