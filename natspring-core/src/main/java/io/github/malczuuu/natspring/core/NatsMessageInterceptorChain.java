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

package io.github.malczuuu.natspring.core;

import io.nats.client.Message;

/**
 * Continues the inbound interceptor chain. Passed to each {@link NatsMessageInterceptor} so it can
 * forward (and optionally replace) the message to the next interceptor or the listener method.
 *
 * @since 0.1.0
 */
public interface NatsMessageInterceptorChain {

  /**
   * Forwards the message to the next interceptor, or to the listener method if no interceptors
   * remain. The message may be replaced with a modified instance.
   *
   * @param message the message to forward
   * @since 0.1.0
   */
  void proceed(Message message);
}
