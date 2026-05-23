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

package io.github.malczuuu.natsify.instrument;

import io.nats.client.ConnectionListener;
import org.jspecify.annotations.Nullable;

final class NoopObserver
    implements JetStreamListenerObserver,
        NatsConnectionObserver,
        NatsErrorObserver,
        NatsListenerObserver {

  static final NoopObserver INSTANCE = new NoopObserver();

  @Override
  public void onReceived(String subject, String stream) {}

  @Override
  public void onSucceeded(String subject, String queue) {}

  @Override
  public void onFailed(String subject, String queue) {}

  @Override
  public void onAcked(String subject, String stream) {}

  @Override
  public void onNacked(String subject, String stream) {}

  @Override
  public void onTerminated(String subject, String stream, @Nullable Exception e) {}

  @Override
  public void onProcessed(String subject, String stream, long durationNanos) {}

  @Override
  public void onConnectionEvent(ConnectionListener.Events event) {}

  @Override
  public void onError(String error) {}

  @Override
  public void onException(Exception exception) {}

  @Override
  public void onSlowConsumerDetected() {}

  @Override
  public void onMessageDiscarded() {}
}
