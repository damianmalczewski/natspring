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
import org.springframework.core.Ordered;

/**
 * Intercepts inbound messages before they are dispatched to a listener method. Implementations are
 * discovered as Spring beans and applied to all listeners in order.
 *
 * <p>Call {@link NatsMessageInterceptorChain#proceed(Message)} to continue the chain. Not calling
 * it drops the message silently. For JetStream listeners, a dropped message will be redelivered
 * unless the interceptor explicitly calls {@code message.ack()} or {@code message.term()} before
 * not proceeding.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface NatsMessageInterceptor extends Ordered {

  /**
   * Intercepts the given message. Call {@link NatsMessageInterceptorChain#proceed(Message)} to
   * forward it to the next interceptor or the listener method. Not calling {@code proceed} drops
   * the message.
   *
   * @param message the inbound message
   * @param chain the chain to continue processing
   * @since 0.1.0
   */
  void intercept(Message message, NatsMessageInterceptorChain chain);

  @Override
  default int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
