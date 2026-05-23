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

import io.github.malczuuu.natsify.instrument.NatsListenerObserver;
import io.nats.client.Connection;
import java.util.ArrayList;
import java.util.List;

public class NatsListenerManager implements ListenerManager {

  private final NatsListenerRegistry natsListenerRegistry;
  private final MessageArgumentResolver argumentResolver;
  private final NatsListenerObserver observer;

  private final List<NatsListenerHandler> handlers = new ArrayList<>();

  public NatsListenerManager(
      NatsListenerRegistry natsListenerRegistry,
      MessageArgumentResolver argumentResolver,
      NatsListenerObserver observer) {
    this.natsListenerRegistry = natsListenerRegistry;
    this.argumentResolver = argumentResolver;
    this.observer = observer;
  }

  @Override
  public synchronized void initialize(Connection connection) throws Exception {
    for (NatsListenerHandle handle : natsListenerRegistry.getHandles()) {
      NatsListenerHandler handler =
          new SubscriptionHandler(
              connection, handle, new NatsListenerInvocation(handle, argumentResolver, observer));
      handlers.add(handler);
      handler.start();
    }
  }

  @Override
  public synchronized void stop() {
    handlers.forEach(NatsListenerHandler::stop);
    handlers.clear();
  }
}
