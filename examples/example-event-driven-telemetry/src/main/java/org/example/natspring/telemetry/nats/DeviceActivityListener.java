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

import io.github.malczuuu.natspring.annotation.NatsListener;
import org.example.natspring.telemetry.core.DeviceInfoService;
import org.example.natspring.telemetry.core.model.RecordActivityCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceActivityListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceActivityListener.class);

  private final DeviceInfoService deviceInfoService;

  public DeviceActivityListener(DeviceInfoService deviceInfoService) {
    this.deviceInfoService = deviceInfoService;
  }

  @NatsListener(subject = "iot.events.processed")
  public void onProcessedEvent(DeviceEventMessage message) {
    deviceInfoService.recordActivity(new RecordActivityCommand(message.deviceId()));
    log.info("Updated last activity for id={}", message.deviceId());
  }
}
