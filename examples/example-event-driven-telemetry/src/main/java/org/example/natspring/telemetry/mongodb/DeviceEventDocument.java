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

package org.example.natspring.telemetry.mongodb;

import java.time.Instant;
import java.util.Map;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullUnmarked
@Document("device_events")
public class DeviceEventDocument {

  @Id
  @Field("_id")
  private String id;

  @Indexed
  @Field("eventId")
  private String eventId;

  @Indexed
  @Field("deviceId")
  private String deviceId;

  @Field("type")
  private String type;

  @Field("payload")
  private Map<String, Object> payload;

  @Field("timestamp")
  private Instant timestamp;

  @Field("receivedAt")
  private Instant receivedAt;

  /** For use by Spring Data MongoDB. */
  protected DeviceEventDocument() {}

  public DeviceEventDocument(
      String eventId,
      String deviceId,
      String type,
      Map<String, Object> payload,
      Instant timestamp,
      Instant receivedAt) {
    this.eventId = eventId;
    this.deviceId = deviceId;
    this.type = type;
    this.payload = payload;
    this.timestamp = timestamp;
    this.receivedAt = receivedAt;
  }

  public String getId() {
    return id;
  }

  public String getEventId() {
    return eventId;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }
}
