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

package io.github.malczuuu.natspring.namastack.outbox;

import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.routing.OutboxRoute;
import io.namastack.outbox.routing.OutboxRouting;
import io.namastack.outbox.routing.selector.OutboxPayloadSelector;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * NATS-specific routing configuration for outbox events.
 *
 * <p>Extends {@link OutboxRouting} with NATS-specific method naming (e.g., {@link
 * #resolveSubject}).
 *
 * <h2>Example (Java)</h2>
 *
 * <pre>{@code
 * @Bean
 * public NatsOutboxRouting natsOutboxRouting() {
 *     return NatsOutboxRouting.builder()
 *         .route(OutboxPayloadSelector.type(OrderEvent.class), route -> route
 *             .target("orders")
 *             .key((payload, metadata) -> ((OrderEvent) payload).getOrderId())
 *         )
 *         .defaults(route -> route.target("domain-events"))
 *         .build();
 * }
 * }</pre>
 *
 * @since 0.3.0
 */
public abstract class NatsOutboxRouting extends OutboxRouting {

  /**
   * Creates a new routing configuration.
   *
   * @param rules ordered list of payload-specific routes
   * @param defaultRule fallback route applied when no specific rule matches, or {@code null} to
   *     skip unmatched payloads
   */
  public NatsOutboxRouting(List<OutboxRoute> rules, @Nullable OutboxRoute defaultRule) {
    super(rules, defaultRule);
  }

  /**
   * Resolves the NATS subject for a given payload and metadata.
   *
   * @throws IllegalStateException if no matching route is found
   */
  public abstract String resolveSubject(Object payload, OutboxRecordMetadata metadata);

  /** Creates a new builder for routing configuration. */
  public static Builder builder() {
    return new DefaultNatsOutboxRouting.Builder();
  }

  /** Builder for {@link NatsOutboxRouting}. */
  public interface Builder {

    /**
     * Adds a payload-specific route that applies only when the selector matches.
     *
     * @param selector the predicate used to match payloads
     * @param routeConfigurer configures the target, key, headers, mapping, and filter for this
     *     route
     * @return this builder
     */
    Builder route(OutboxPayloadSelector selector, Consumer<OutboxRoute.Builder> routeConfigurer);

    /**
     * Sets the fallback route used when no payload-specific route matches.
     *
     * @param routeConfigurer configures the default target, key, headers, mapping, and filter
     * @return this builder
     */
    Builder defaults(Consumer<OutboxRoute.Builder> routeConfigurer);

    /**
     * Builds the {@link NatsOutboxRouting} instance.
     *
     * @return the configured routing
     */
    NatsOutboxRouting build();
  }
}
