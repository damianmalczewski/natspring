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

public class DeadLetterRepositoryCustomImpl implements DeadLetterRepositoryCustom {

  private final MongoOperations mongoOperations;

  public DeadLetterRepositoryCustomImpl(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  @Override
  public void upsertByStreamId(DeadLetterDocument deadLetter) {
    mongoOperations.upsert(
        Query.query(Criteria.where("streamId").is(deadLetter.getStreamId())),
        new Update()
            .setOnInsert("_id", new ObjectId().toHexString())
            .setOnInsert("streamId", deadLetter.getStreamId())
            .set("originalSubject", deadLetter.getOriginalSubject())
            .set("rawPayload", deadLetter.getRawPayload())
            .set("reason", deadLetter.getReason())
            .set("originalStream", deadLetter.getOriginalStream())
            .set("originalDurable", deadLetter.getOriginalDurable())
            .setOnInsert("deadLetteredAt", deadLetter.getDeadLetteredAt()),
        DeadLetterDocument.class);
  }
}
