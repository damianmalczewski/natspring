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

import io.github.malczuuu.natspring.core.NatsOperations;
import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.nats.client.impl.Headers;
import io.nats.client.support.NatsJetStreamConstants;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outbox handler that publishes payloads to NATS subjects.
 *
 * <p>Uses {@link NatsOutboxRouting} to determine the subject, headers, payload mapping, and
 * filtering for each payload. Payload serialization follows the same rules as {@link
 * NatsOperations}: {@code byte[]} is sent as-is, {@code String} is UTF-8 encoded, and any other
 * type is JSON-serialized via Jackson.
 *
 * <p>This handler is auto-configured. Provide a custom {@link NatsOutboxRouting} bean to override
 * the default routing behavior.
 *
 * <h2>Example Custom Routing</h2>
 *
 * <pre>{@code
 * @Bean
 * public NatsOutboxRouting natsOutboxRouting() {
 *     return NatsOutboxRouting.builder()
 *         .route(OutboxPayloadSelector.type(OrderEvent.class), route -> route
 *             .target("orders")
 *             .key((payload, metadata) -> ((OrderEvent) payload).getOrderId())
 *             .mapping((payload, metadata) -> ((OrderEvent) payload).toPublicEvent())
 *             .filter((payload, metadata) -> !((OrderEvent) payload).getStatus().equals("CANCELLED"))
 *         )
 *         .defaults(route -> route.target("domain-events"))
 *         .build();
 * }
 * }</pre>
 *
 * @since 0.3.0
 */
public class DefaultNatsOutboxHandler implements NatsOutboxHandler {

  private static final Logger log = LoggerFactory.getLogger(DefaultNatsOutboxHandler.class);

  private final NatsOperations natsOperations;
  private final NatsOutboxRouting routing;

  /**
   * Creates a new handler.
   *
   * @param natsOperations the operations used to publish messages
   * @param routing the routing configuration
   */
  public DefaultNatsOutboxHandler(NatsOperations natsOperations, NatsOutboxRouting routing) {
    this.natsOperations = natsOperations;
    this.routing = routing;
  }

  /**
   * Returns {@code true} if the routing configuration does not filter out the given payload.
   *
   * @param payload the outbox payload
   * @param metadata the outbox record metadata
   * @return {@code true} if the payload passes the routing filter
   */
  @Override
  public boolean supports(Object payload, OutboxRecordMetadata metadata) {
    return routing.shouldExternalize(payload, metadata);
  }

  /**
   * Publishes the payload to the NATS subject resolved by the routing configuration.
   *
   * <p>If the routing filter excludes the payload, the message is silently skipped.
   *
   * @param payload the outbox payload
   * @param metadata the outbox record metadata
   */
  @Override
  public void handle(Object payload, OutboxRecordMetadata metadata) {
    if (!supports(payload, metadata)) {
      log.debug("Skipping outbox record due to filter: handlerId={}", metadata.getHandlerId());
      return;
    }

    String subject = routing.resolveSubject(payload, metadata);
    Map<String, String> headerMap = routing.buildHeaders(payload, metadata);
    Object mappedPayload = routing.mapPayload(payload, metadata);
    Headers headers = toNatsHeaders(headerMap);

    String key = routing.extractKey(payload, metadata);
    if (key != null) {
      headers.put(NatsJetStreamConstants.MSG_ID_HDR, key);
    }

    log.debug(
        "Sending outbox record to NATS: subject={}, handlerId={}",
        subject,
        metadata.getHandlerId());

    publish(subject, headers, mappedPayload);

    log.debug(
        "Successfully published outbox record to NATS: subject={}, handlerId={}",
        subject,
        metadata.getHandlerId());
  }

  private void publish(String subject, Headers headers, Object payload) {
    if (payload instanceof byte[] bytes) {
      natsOperations.publish(subject, headers, bytes);
    } else if (payload instanceof String str) {
      natsOperations.publish(subject, headers, str);
    } else {
      natsOperations.publish(subject, headers, payload);
    }
  }

  private Headers toNatsHeaders(Map<String, String> headerMap) {
    Headers headers = new Headers();
    headerMap.forEach(headers::add);
    return headers;
  }
}
