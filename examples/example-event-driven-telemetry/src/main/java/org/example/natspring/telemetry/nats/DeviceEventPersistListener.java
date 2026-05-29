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
import io.github.malczuuu.natspring.annotation.NatsHeader;
import java.time.Instant;
import org.example.natspring.telemetry.core.DeviceEventService;
import org.example.natspring.telemetry.core.model.DeviceEventCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventPersistListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventPersistListener.class);

  private final DeviceEventService deviceEventService;

  public DeviceEventPersistListener(DeviceEventService deviceEventService) {
    this.deviceEventService = deviceEventService;
  }

  @JetStreamListener(
      subject = "iot.events.processed",
      stream = "IOT_PROCESSED",
      durable = "iot-event-persister")
  public void onProcessedEvent(
      DeviceEventMessage message, @NatsHeader("X-Event-Id") @Nullable String eventId) {
    if (eventId == null) {
      throw new IllegalArgumentException("Missing event id");
    }
    deviceEventService.persistEvent(
        new DeviceEventCommand(
            eventId,
            message.deviceId(),
            message.type(),
            message.payload(),
            message.timestamp(),
            Instant.now()));
    log.info("Persisted IoT event eventId={}, id={}", eventId, message.deviceId());
  }
}
