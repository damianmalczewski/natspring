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

import io.namastack.outbox.handler.OutboxHandler;
import io.namastack.outbox.handler.OutboxRecordMetadata;

/**
 * Outbox handler that publishes payloads to NATS subjects.
 *
 * @since 0.3.0
 */
public interface NatsOutboxHandler extends OutboxHandler {

  /**
   * Returns {@code true} if the routing configuration does not filter out the given payload.
   *
   * @param payload the outbox payload
   * @param metadata the outbox record metadata
   * @return {@code true} if the payload passes the routing filter
   */
  @Override
  default boolean supports(Object payload, OutboxRecordMetadata metadata) {
    return true;
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
  void handle(Object payload, OutboxRecordMetadata metadata);
}
