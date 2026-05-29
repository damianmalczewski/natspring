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

package org.example.natspring.telemetry.core;

import java.util.List;
import org.example.natspring.telemetry.core.model.DeviceEventCommand;
import org.example.natspring.telemetry.core.model.DeviceEventModel;
import org.example.natspring.telemetry.mongodb.DeviceEventDocument;
import org.example.natspring.telemetry.mongodb.DeviceEventRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceEventService {

  private final DeviceEventRepository deviceEventRepository;

  public DeviceEventService(DeviceEventRepository deviceEventRepository) {
    this.deviceEventRepository = deviceEventRepository;
  }

  public void persistEvent(DeviceEventCommand command) {
    DeviceEventDocument deviceEvent =
        new DeviceEventDocument(
            command.eventId(),
            command.deviceId(),
            command.type(),
            command.payload(),
            command.timestamp(),
            command.receivedAt());
    deviceEventRepository.upsertByEventId(deviceEvent);
  }

  public List<DeviceEventModel> findByDeviceId(String deviceId) {
    return deviceEventRepository.findByDeviceId(deviceId).stream()
        .map(this::toDeviceEventModel)
        .toList();
  }

  private DeviceEventModel toDeviceEventModel(DeviceEventDocument entity) {
    return new DeviceEventModel(
        entity.getEventId(),
        entity.getDeviceId(),
        entity.getType(),
        entity.getPayload(),
        entity.getTimestamp(),
        entity.getReceivedAt());
  }
}
