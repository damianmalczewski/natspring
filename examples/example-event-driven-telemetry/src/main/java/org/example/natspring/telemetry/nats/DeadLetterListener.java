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

package org.example.natspring.telemetry.nats;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import org.example.natspring.telemetry.core.DeadLetterService;
import org.example.natspring.telemetry.core.model.DeadLetterCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterListener {

  private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

  private final DeadLetterService deadLetterService;
  private final StreamSequenceSupport streamSequence;
  private final Clock clock;

  public DeadLetterListener(
      DeadLetterService deadLetterService, StreamSequenceSupport streamSequence, Clock clock) {
    this.deadLetterService = deadLetterService;
    this.streamSequence = streamSequence;
    this.clock = clock;
  }

  @JetStreamListener(
      subject = "iot.events.deadletter",
      stream = "IOT_DLQ",
      durable = "iot-dlq-persister")
  public void onDeadLetter(Message message, NatsJetStreamMetaData meta) {
    String streamId = streamSequence.build(meta.getStream(), meta.streamSequence());
    String rawPayload =
        message.getData() != null ? new String(message.getData(), StandardCharsets.UTF_8) : "";

    String originalSubject = null;
    String reason = null;
    String originalStream = null;
    String originalDurable = null;
    Instant deadLetteredAt = clock.instant();

    Headers headers = message.getHeaders();
    if (headers != null) {
      originalSubject = headers.getFirst("X-Dead-Letter-Subject");
      reason = headers.getFirst("X-Dead-Letter-Reason");
      originalStream = headers.getFirst("X-Dead-Letter-Stream");
      originalDurable = headers.getFirst("X-Dead-Letter-Durable");
      String timestampStr = headers.getFirst("X-Dead-Letter-Timestamp");
      deadLetteredAt = timestampStr != null ? Instant.parse(timestampStr) : deadLetteredAt;
    }

    DeadLetterCommand command =
        new DeadLetterCommand(
            streamId,
            originalSubject,
            rawPayload,
            reason,
            originalStream,
            originalDurable,
            deadLetteredAt);

    deadLetterService.persist(command);
    log.info("Persisted dead letter streamId={}, originalSubject={}", streamId, originalSubject);
  }
}
