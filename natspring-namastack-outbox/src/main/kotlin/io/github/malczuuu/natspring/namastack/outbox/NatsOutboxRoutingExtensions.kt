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

package io.github.malczuuu.natspring.namastack.outbox

import io.namastack.outbox.routing.OutboxRoute
import io.namastack.outbox.routing.selector.OutboxPayloadSelector

/**
 * Creates a [NatsOutboxRouting] configuration using Kotlin DSL.
 *
 * ## Example
 *
 * ```kotlin
 * @Bean
 * fun natsOutboxRouting() = natsOutboxRouting {
 *     route(OutboxPayloadSelector.type(OrderEvent::class.java)) {
 *         target("orders")
 *         key { payload, _ -> (payload as OrderEvent).orderId }
 *         headers { _, metadata -> metadata.context }
 *         mapping { payload, _ -> (payload as OrderEvent).toPublicEvent() }
 *         filter { payload, _ -> (payload as OrderEvent).status != "CANCELLED" }
 *     }
 *     defaults {
 *         target("domain-events")
 *     }
 * }
 * ```
 *
 * @param configurer lambda to configure routes and defaults
 * @return a [NatsOutboxRouting] instance
 * @since 0.3.0
 */
fun natsOutboxRouting(configurer: NatsOutboxRoutingBuilder.() -> Unit): NatsOutboxRouting {
    val builder = NatsOutboxRoutingBuilder()
    builder.configurer()
    return builder.build()
}

/**
 * Kotlin DSL builder for [NatsOutboxRouting].
 *
 * Wraps [NatsOutboxRouting.Builder] to expose receiver-lambda overloads of [route] and [defaults],
 * enabling idiomatic Kotlin DSL syntax without needing explicit `it.` parameter references.
 *
 * @since 0.3.0
 */
class NatsOutboxRoutingBuilder {
    private val delegate = NatsOutboxRouting.builder()

    /**
     * Adds a payload-specific route that applies only when the [selector] matches.
     *
     * @param selector the predicate used to match payloads
     * @param routeConfigurer configures the target, key, headers, mapping, and filter for this route
     */
    fun route(
        selector: OutboxPayloadSelector,
        routeConfigurer: OutboxRoute.Builder.() -> Unit,
    ) {
        delegate.route(selector) { builder -> builder.routeConfigurer() }
    }

    /**
     * Sets the fallback route used when no payload-specific route matches.
     *
     * @param routeConfigurer configures the default target, key, headers, mapping, and filter
     */
    fun defaults(routeConfigurer: OutboxRoute.Builder.() -> Unit) {
        delegate.defaults { builder -> builder.routeConfigurer() }
    }

    /** Builds the [NatsOutboxRouting] instance. */
    fun build(): NatsOutboxRouting = delegate.build()
}
