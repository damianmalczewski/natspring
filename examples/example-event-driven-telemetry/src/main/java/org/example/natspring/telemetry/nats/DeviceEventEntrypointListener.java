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
import io.github.malczuuu.natspring.core.NatsOperations;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventEntrypointListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventEntrypointListener.class);

  private final NatsOperations natsOperations;
  private final StreamSequenceSupport streamSequence;

  public DeviceEventEntrypointListener(
      NatsOperations natsOperations, StreamSequenceSupport streamSequence) {
    this.natsOperations = natsOperations;
    this.streamSequence = streamSequence;
  }

  @JetStreamListener(
      subject = "iot.events.raw",
      stream = "IOT_RAW",
      durable = "iot-raw-processor",
      maxDeliveries = 5,
      deadLetterSubject = "iot.events.deadletter")
  public void onRawEvent(DeviceEventMessage message, NatsJetStreamMetaData meta) {
    validate(message);
    Headers headers = new Headers();
    headers.add("X-Event-Id", streamSequence.build(meta.getStream(), meta.streamSequence()));
    natsOperations.publish("iot.events.processed", headers, message);
    log.info("Processed IoT event id={}, type={}", message.deviceId(), message.type());
  }

  private static void validate(DeviceEventMessage message) {
    if (message.deviceId() == null || message.deviceId().isBlank()) {
      throw new IllegalArgumentException("id must not be blank");
    }
    if (message.type() == null || message.type().isBlank()) {
      throw new IllegalArgumentException("type must not be blank");
    }
    if (message.timestamp() == null) {
      throw new IllegalArgumentException("timestamp must not be null");
    }
  }
}
