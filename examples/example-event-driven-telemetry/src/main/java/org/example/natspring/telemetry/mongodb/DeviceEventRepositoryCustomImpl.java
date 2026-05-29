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

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class DeviceEventRepositoryCustomImpl implements DeviceEventRepositoryCustom {

  private final MongoOperations mongoOperations;

  public DeviceEventRepositoryCustomImpl(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  @Override
  public void upsertByEventId(DeviceEventDocument deviceEvent) {
    mongoOperations.upsert(
        Query.query(Criteria.where("eventId").is(deviceEvent.getEventId())),
        new Update()
            .setOnInsert("_id", new ObjectId().toHexString())
            .setOnInsert("eventId", deviceEvent.getEventId())
            .setOnInsert("deviceId", deviceEvent.getDeviceId())
            .setOnInsert("type", deviceEvent.getType())
            .setOnInsert("payload", deviceEvent.getPayload())
            .setOnInsert("timestamp", deviceEvent.getTimestamp())
            .setOnInsert("receivedAt", deviceEvent.getReceivedAt()),
        DeviceEventDocument.class);
  }
}
