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
import org.example.natspring.telemetry.core.DeviceInfoService;
import org.example.natspring.telemetry.core.model.IncrementEventCountCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventCounterListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventCounterListener.class);

  private final DeviceInfoService deviceInfoService;

  public DeviceEventCounterListener(DeviceInfoService deviceInfoService) {
    this.deviceInfoService = deviceInfoService;
  }

  @JetStreamListener(
      subject = "iot.events.processed",
      stream = "IOT_PROCESSED",
      durable = "iot-device-counter")
  public void onProcessedEvent(DeviceEventMessage message) {
    deviceInfoService.incrementEventCount(new IncrementEventCountCommand(message.deviceId()));
    log.info("Incremented event count for id={}", message.deviceId());
  }
}
