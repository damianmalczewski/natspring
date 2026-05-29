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

import io.namastack.outbox.handler.OutboxRecordMetadata
import io.namastack.outbox.routing.selector.OutboxPayloadSelector
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class NatsOutboxRoutingDslTest {
    private val metadata =
        OutboxRecordMetadata(
            key = "order-123",
            handlerId = "test-handler",
            createdAt = Instant.now(),
            context = mapOf("tenant" to "acme"),
        )

    @Nested
    inner class NatsOutboxRoutingFunction {
        @Test
        fun `natsOutboxRouting creates routing with route`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target("strings")
                    }
                }

            assertThat(routing.resolveSubject("test", metadata)).isEqualTo("strings")
        }

        @Test
        fun `natsOutboxRouting creates routing with defaults`() {
            val routing =
                natsOutboxRouting {
                    defaults {
                        target("default-subject")
                    }
                }

            assertThat(routing.resolveSubject("any-payload", metadata)).isEqualTo("default-subject")
        }

        @Test
        fun `natsOutboxRouting supports all route options`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target("strings")
                        key { payload, _ -> "key-$payload" }
                        headers { _, meta -> mapOf("tenant" to meta.context["tenant"]!!) }
                        mapping { payload, _ -> (payload as String).uppercase() }
                        filter { payload, _ -> (payload as String).isNotEmpty() }
                    }
                }

            assertThat(routing.resolveSubject("test", metadata)).isEqualTo("strings")
            assertThat(routing.extractKey("test", metadata)).isEqualTo("key-test")
            assertThat(routing.buildHeaders("test", metadata)).containsEntry("tenant", "acme")
            assertThat(routing.mapPayload("test", metadata)).isEqualTo("TEST")
            assertThat(routing.shouldExternalize("test", metadata)).isTrue()
            assertThat(routing.shouldExternalize("", metadata)).isFalse()
        }

        @Test
        fun `natsOutboxRouting supports multiple routes`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target("strings")
                    }
                    route(OutboxPayloadSelector.type(Int::class.javaObjectType)) {
                        target("ints")
                    }
                }

            assertThat(routing.resolveSubject("test", metadata)).isEqualTo("strings")
            assertThat(routing.resolveSubject(123, metadata)).isEqualTo("ints")
        }
    }

    @Nested
    inner class ResolveSubject {
        @Test
        fun `resolveSubject throws when no matching route`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(Int::class.javaObjectType)) {
                        target("ints")
                    }
                }

            assertThatThrownBy { routing.resolveSubject("string", metadata) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("No routing rule found")
        }

        @Test
        fun `resolveSubject supports dynamic subject`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target { payload, _ -> "events.$payload" }
                    }
                }

            assertThat(routing.resolveSubject("orders", metadata)).isEqualTo("events.orders")
        }
    }

    @Nested
    inner class RoutePrecedence {
        @Test
        fun `first matching route wins`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target("first")
                    }
                    route(OutboxPayloadSelector.predicate { _, _ -> true }) {
                        target("second")
                    }
                }

            assertThat(routing.resolveSubject("test", metadata)).isEqualTo("first")
        }

        @Test
        fun `defaults used when no route matches`() {
            val routing =
                natsOutboxRouting {
                    route(OutboxPayloadSelector.type(Int::class.javaObjectType)) {
                        target("ints")
                    }
                    defaults {
                        target("default-subject")
                    }
                }

            assertThat(routing.resolveSubject("string", metadata)).isEqualTo("default-subject")
        }

        @Test
        fun `specific route takes precedence over defaults`() {
            val routing =
                natsOutboxRouting {
                    defaults {
                        target("default-subject")
                    }
                    route(OutboxPayloadSelector.type(String::class.java)) {
                        target("strings")
                    }
                }

            assertThat(routing.resolveSubject("test", metadata)).isEqualTo("strings")
        }
    }
}
