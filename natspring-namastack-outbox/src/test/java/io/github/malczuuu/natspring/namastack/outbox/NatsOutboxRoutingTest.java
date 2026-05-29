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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.routing.selector.OutboxPayloadSelector;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NatsOutboxRoutingTest {

  private static final OutboxRecordMetadata METADATA =
      new OutboxRecordMetadata(
          "order-123", "test-handler", Instant.now(), Map.of("tenant", "acme"), 0);

  @Nested
  class BuilderTests {

    @Test
    void givenDefaultsConfigured_whenResolveSubject_thenReturnsDefaultSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("default-subject")).build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("default-subject");
    }

    @Test
    void givenRouteConfigured_whenResolveSubject_thenReturnsRouteSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(String.class), route -> route.target("strings"))
              .build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("strings");
    }

    @Test
    void givenMultipleRoutes_whenResolveSubject_thenFirstMatchWins() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(String.class), route -> route.target("first"))
              .route(OutboxPayloadSelector.all(), route -> route.target("second"))
              .build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("first");
    }

    @Test
    void givenRouteAndDefault_whenNoRouteMatches_thenUsesDefault() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(Integer.class), route -> route.target("ints"))
              .defaults(route -> route.target("default-subject"))
              .build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("default-subject");
    }

    @Test
    void givenBuilderMethods_whenChained_thenReturnsSameBuilder() {
      NatsOutboxRouting.Builder builder = NatsOutboxRouting.builder();

      NatsOutboxRouting.Builder result =
          builder
              .route(OutboxPayloadSelector.type(String.class), route -> route.target("strings"))
              .defaults(route -> route.target("default"));

      assertThat(result).isSameAs(builder);
    }

    @Test
    void givenMultipleTypeRoutes_whenHandleDifferentTypes_thenRoutesToCorrectSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(String.class), route -> route.target("strings"))
              .route(OutboxPayloadSelector.type(Integer.class), route -> route.target("ints"))
              .build();

      assertThat(routing.resolveSubject("test", METADATA)).isEqualTo("strings");
      assertThat(routing.resolveSubject(123, METADATA)).isEqualTo("ints");
    }
  }

  @Nested
  class ResolveSubjectTests {

    @Test
    void givenMatchingRoute_whenResolveSubject_thenDelegatesToResolveTarget() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(String.class), route -> route.target("my-subject"))
              .build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("my-subject");
      assertThat(routing.resolveTarget("payload", METADATA)).isEqualTo("my-subject");
    }

    @Test
    void givenNoMatchingRoute_whenResolveSubject_thenThrowsIllegalStateException() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(Integer.class), route -> route.target("ints"))
              .build();

      assertThatThrownBy(() -> routing.resolveSubject("payload", METADATA))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No routing rule found");
    }

    @Test
    void givenDynamicTargetResolver_whenResolveSubject_thenUsesResolver() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route ->
                      route.target(
                          (payload, meta) ->
                              "events." + payload.getClass().getSimpleName().toLowerCase()))
              .build();

      assertThat(routing.resolveSubject("payload", METADATA)).isEqualTo("events.string");
    }
  }

  @Nested
  class InheritedMethodTests {

    @Test
    void givenKeyConfigured_whenExtractKey_thenReturnsKey() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(String.class),
                  route -> {
                    route.target("strings");
                    route.key((payload, meta) -> "key-" + payload);
                  })
              .build();

      assertThat(routing.extractKey("test", METADATA)).isEqualTo("key-test");
    }

    @Test
    void givenHeadersConfigured_whenBuildHeaders_thenReturnsHeaders() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(String.class),
                  route -> {
                    route.target("strings");
                    route.headers((payload, meta) -> meta.getContext());
                  })
              .build();

      assertThat(routing.buildHeaders("test", METADATA)).containsEntry("tenant", "acme");
    }

    @Test
    void givenMappingConfigured_whenMapPayload_thenReturnsMappedPayload() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(String.class),
                  route -> {
                    route.target("strings");
                    route.mapping((payload, meta) -> ((String) payload).toUpperCase());
                  })
              .build();

      assertThat(routing.mapPayload("test", METADATA)).isEqualTo("TEST");
    }

    @Test
    void givenFilterConfigured_whenShouldExternalize_thenReturnsFilterResult() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(String.class),
                  route -> {
                    route.target("strings");
                    route.filter((payload, meta) -> !payload.equals("skip"));
                  })
              .build();

      assertThat(routing.shouldExternalize("test", METADATA)).isTrue();
      assertThat(routing.shouldExternalize("skip", METADATA)).isFalse();
    }
  }
}
