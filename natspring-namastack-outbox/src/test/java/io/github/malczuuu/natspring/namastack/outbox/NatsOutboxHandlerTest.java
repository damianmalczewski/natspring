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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.malczuuu.natspring.core.NatsOperations;
import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.routing.selector.OutboxPayloadSelector;
import io.nats.client.impl.Headers;
import io.nats.client.support.NatsJetStreamConstants;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NatsOutboxHandlerTest {

  record TestPayload(String value) {}

  private NatsOperations natsOperations;

  private static final OutboxRecordMetadata METADATA =
      new OutboxRecordMetadata(
          "order-123", "test-handler", Instant.now(), Map.of("tenant", "acme"), 0);

  @BeforeEach
  void beforeEach() {
    natsOperations = mock(NatsOperations.class);
  }

  @Nested
  class SupportsTests {

    @Test
    void givenFilterExcludesPayload_whenSupports_thenReturnsFalse() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.filter((payload, meta) -> !payload.equals(new TestPayload("skip")));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      assertThat(handler.supports(new TestPayload("skip"), METADATA)).isFalse();
    }

    @Test
    void givenFilterAcceptsPayload_whenSupports_thenReturnsTrue() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("events")).build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      assertThat(handler.supports(new TestPayload("any"), METADATA)).isTrue();
    }
  }

  @Nested
  class HandleTests {

    @Test
    void givenDefaultRouting_whenHandle_thenPublishesToResolvedSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("default-subject")).build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test"), METADATA);

      ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
      verify(natsOperations)
          .publish(subjectCaptor.capture(), any(Headers.class), any(Object.class));
      assertThat(subjectCaptor.getValue()).isEqualTo("default-subject");
    }

    @Test
    void givenHeadersInRouting_whenHandle_thenPublishesWithHeaders() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.headers((payload, meta) -> meta.getContext());
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test"), METADATA);

      ArgumentCaptor<Headers> headersCaptor = ArgumentCaptor.forClass(Headers.class);
      verify(natsOperations).publish(anyString(), headersCaptor.capture(), any(Object.class));
      assertThat(headersCaptor.getValue().getFirst("tenant")).isEqualTo("acme");
    }

    @Test
    void givenMappingInRouting_whenHandle_thenPublishesMappedPayload() {
      record MappedEvent(String value) {}

      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.mapping(
                        (payload, meta) ->
                            new MappedEvent(((TestPayload) payload).value().toUpperCase()));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test-payload"), METADATA);

      ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
      verify(natsOperations).publish(anyString(), any(Headers.class), payloadCaptor.capture());
      assertThat(payloadCaptor.getValue()).isEqualTo(new MappedEvent("TEST-PAYLOAD"));
    }

    @Test
    void givenFilterExcludesPayload_whenHandle_thenSkipsPublish() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.filter((payload, meta) -> !payload.equals(new TestPayload("skip")));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("skip"), METADATA);

      verifyNoInteractions(natsOperations);
    }

    @Test
    void givenFilterAcceptsPayload_whenHandle_thenPublishes() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.filter((payload, meta) -> !payload.equals(new TestPayload("skip")));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("send"), METADATA);

      verify(natsOperations).publish(anyString(), any(Headers.class), any(Object.class));
    }

    @Test
    void givenTypeRouting_whenHandleMatchingType_thenPublishesToCorrectSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(TestPayload.class), route -> route.target("events"))
              .route(OutboxPayloadSelector.type(Integer.class), route -> route.target("ints"))
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test"), METADATA);

      ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
      verify(natsOperations)
          .publish(subjectCaptor.capture(), any(Headers.class), any(Object.class));
      assertThat(subjectCaptor.getValue()).isEqualTo("events");
    }

    @Test
    void givenNoMatchingRoute_whenHandle_thenSkipsPublish() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(OutboxPayloadSelector.type(Integer.class), route -> route.target("ints"))
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("unmatched"), METADATA);

      verifyNoInteractions(natsOperations);
    }

    @Test
    void givenBytesPayload_whenHandle_thenPublishesBytesDirectly() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("events")).build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      byte[] bytes = {1, 2, 3};
      handler.handle(bytes, METADATA);

      ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);
      verify(natsOperations).publish(anyString(), any(Headers.class), bodyCaptor.capture());
      assertThat(bodyCaptor.getValue()).isEqualTo(bytes);
    }

    @Test
    void givenStringPayload_whenHandle_thenPublishesStringDirectly() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("events")).build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle("hello", METADATA);

      ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
      verify(natsOperations).publish(anyString(), any(Headers.class), bodyCaptor.capture());
      assertThat(bodyCaptor.getValue()).isEqualTo("hello");
    }

    @Test
    void givenDefaultRouting_whenHandle_thenPublishesWithMetadataKeyAsMsgId() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder().defaults(route -> route.target("events")).build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test"), METADATA);

      ArgumentCaptor<Headers> headersCaptor = ArgumentCaptor.forClass(Headers.class);
      verify(natsOperations).publish(anyString(), headersCaptor.capture(), any(Object.class));
      assertThat(headersCaptor.getValue().getFirst(NatsJetStreamConstants.MSG_ID_HDR))
          .isEqualTo(METADATA.getKey());
    }

    @Test
    void givenCustomKeyInRouting_whenHandle_thenPublishesWithCustomMsgId() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.key((payload, meta) -> ((TestPayload) payload).value());
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("custom-key-value"), METADATA);

      ArgumentCaptor<Headers> headersCaptor = ArgumentCaptor.forClass(Headers.class);
      verify(natsOperations).publish(anyString(), headersCaptor.capture(), any(Object.class));
      assertThat(headersCaptor.getValue().getFirst(NatsJetStreamConstants.MSG_ID_HDR))
          .isEqualTo("custom-key-value");
    }

    @Test
    void givenNullKeyFromRouting_whenHandle_thenPublishesWithoutMsgIdHeader() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route -> {
                    route.target("events");
                    route.key((payload, meta) -> null);
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("test"), METADATA);

      ArgumentCaptor<Headers> headersCaptor = ArgumentCaptor.forClass(Headers.class);
      verify(natsOperations).publish(anyString(), headersCaptor.capture(), any(Object.class));
      assertThat(headersCaptor.getValue().getFirst(NatsJetStreamConstants.MSG_ID_HDR)).isNull();
    }

    @Test
    void givenDynamicSubjectRouting_whenHandle_thenPublishesToDynamicSubject() {
      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .defaults(
                  route ->
                      route.target(
                          (payload, meta) ->
                              "events." + payload.getClass().getSimpleName().toLowerCase()))
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new TestPayload("data"), METADATA);

      ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
      verify(natsOperations)
          .publish(subjectCaptor.capture(), any(Headers.class), any(Object.class));
      assertThat(subjectCaptor.getValue()).isEqualTo("events.testpayload");
    }
  }

  @Nested
  class FullConfigurationTests {

    @Test
    void givenAllRoutingOptions_whenHandle_thenAppliesAllOptions() {
      record OrderEvent(String orderId, String status) {}

      record PublicOrderEvent(String id) {}

      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(OrderEvent.class),
                  route -> {
                    route.target("orders");
                    route.headers(
                        (payload, meta) ->
                            Map.of("tenant", meta.getContext().getOrDefault("tenant", "unknown")));
                    route.mapping(
                        (payload, meta) -> new PublicOrderEvent(((OrderEvent) payload).orderId()));
                    route.filter(
                        (payload, meta) -> !((OrderEvent) payload).status().equals("CANCELLED"));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new OrderEvent("order-456", "CREATED"), METADATA);

      ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Headers> headersCaptor = ArgumentCaptor.forClass(Headers.class);
      ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
      verify(natsOperations)
          .publish(subjectCaptor.capture(), headersCaptor.capture(), payloadCaptor.capture());
      assertThat(subjectCaptor.getValue()).isEqualTo("orders");
      assertThat(headersCaptor.getValue().getFirst("tenant")).isEqualTo("acme");
      assertThat(payloadCaptor.getValue()).isEqualTo(new PublicOrderEvent("order-456"));
    }

    @Test
    void givenCancelledOrder_whenHandle_thenSkipsPublish() {
      record OrderEvent(String orderId, String status) {}

      NatsOutboxRouting routing =
          NatsOutboxRouting.builder()
              .route(
                  OutboxPayloadSelector.type(OrderEvent.class),
                  route -> {
                    route.target("orders");
                    route.filter(
                        (payload, meta) -> !((OrderEvent) payload).status().equals("CANCELLED"));
                  })
              .build();
      NatsOutboxHandler handler = new DefaultNatsOutboxHandler(natsOperations, routing);

      handler.handle(new OrderEvent("order-789", "CANCELLED"), METADATA);

      verifyNoInteractions(natsOperations);
    }
  }
}
