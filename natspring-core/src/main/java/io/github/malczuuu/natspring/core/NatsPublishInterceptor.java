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
 * Intercepts outbound messages before they are published via {@link NatsOperations}.
 * Implementations are discovered as Spring beans and applied to all publishes in order.
 *
 * <p>All publish variants (raw bytes, string, object) funnel through this chain after
 * serialization, so interceptors always receive a fully-built {@link Message}.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface NatsPublishInterceptor extends Ordered {

  /**
   * Intercepts the given outbound message. Use {@link NatsPublishInterceptorChain#proceed(Message)}
   * to forward it to the next interceptor or the NATS connection. Not calling {@code proceed}
   * suppresses the publishing.
   *
   * @param message the outbound message, fully built with subject, headers, and serialized body
   * @param chain the chain to continue publishing
   * @since 0.1.0
   */
  void intercept(Message message, NatsPublishInterceptorChain chain);

  @Override
  default int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
