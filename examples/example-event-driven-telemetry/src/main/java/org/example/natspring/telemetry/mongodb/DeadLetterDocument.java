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
import org.jspecify.annotations.NullUnmarked;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullUnmarked
@Document("dead_letters")
public class DeadLetterDocument {

  @Id
  @Field("_id")
  private String id;

  @Field("streamId")
  @Indexed
  private String streamId;

  @Field("originalSubject")
  private String originalSubject;

  @Field("rawPayload")
  private String rawPayload;

  @Field("reason")
  private String reason;

  @Field("originalStream")
  private String originalStream;

  @Field("originalDurable")
  private String originalDurable;

  @Field("deadLetteredAt")
  private Instant deadLetteredAt;

  /** For use by Spring Data MongoDB. */
  protected DeadLetterDocument() {}

  public DeadLetterDocument(
      String streamId,
      String originalSubject,
      String rawPayload,
      String reason,
      String originalStream,
      String originalDurable,
      Instant deadLetteredAt) {
    this.streamId = streamId;
    this.originalSubject = originalSubject;
    this.rawPayload = rawPayload;
    this.reason = reason;
    this.originalStream = originalStream;
    this.originalDurable = originalDurable;
    this.deadLetteredAt = deadLetteredAt;
  }

  public String getId() {
    return id;
  }

  public String getStreamId() {
    return streamId;
  }

  public String getOriginalSubject() {
    return originalSubject;
  }

  public String getRawPayload() {
    return rawPayload;
  }

  public String getReason() {
    return reason;
  }

  public String getOriginalStream() {
    return originalStream;
  }

  public String getOriginalDurable() {
    return originalDurable;
  }

  public Instant getDeadLetteredAt() {
    return deadLetteredAt;
  }
}
