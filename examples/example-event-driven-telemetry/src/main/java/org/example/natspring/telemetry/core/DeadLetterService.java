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
import java.util.Optional;
import org.example.natspring.telemetry.core.model.DeadLetterCommand;
import org.example.natspring.telemetry.core.model.DeadLetterModel;
import org.example.natspring.telemetry.mongodb.DeadLetterDocument;
import org.example.natspring.telemetry.mongodb.DeadLetterRepository;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterService {

  private static final int PAYLOAD_PREVIEW_LENGTH = 32;

  private final DeadLetterRepository deadLetterRepository;

  public DeadLetterService(DeadLetterRepository deadLetterRepository) {
    this.deadLetterRepository = deadLetterRepository;
  }

  public void persist(DeadLetterCommand command) {
    deadLetterRepository.upsertByStreamId(
        new DeadLetterDocument(
            command.streamId(),
            command.originalSubject(),
            command.rawPayload(),
            command.reason(),
            command.originalStream(),
            command.originalDurable(),
            command.deadLetteredAt()));
  }

  public List<DeadLetterModel> findAll() {
    return deadLetterRepository.findAll().stream().map(this::toPreview).toList();
  }

  public Optional<DeadLetterModel> findById(String id) {
    return deadLetterRepository.findByStreamId(id).map(this::toFullModel);
  }

  private DeadLetterModel toPreview(DeadLetterDocument entity) {
    return new DeadLetterModel(
        entity.getStreamId(),
        entity.getOriginalSubject(),
        truncate(entity.getRawPayload()),
        entity.getReason(),
        entity.getOriginalStream(),
        entity.getOriginalDurable(),
        entity.getDeadLetteredAt());
  }

  private DeadLetterModel toFullModel(DeadLetterDocument entity) {
    return new DeadLetterModel(
        entity.getStreamId(),
        entity.getOriginalSubject(),
        entity.getRawPayload(),
        entity.getReason(),
        entity.getOriginalStream(),
        entity.getOriginalDurable(),
        entity.getDeadLetteredAt());
  }

  private static String truncate(String value) {
    if (value.length() <= PAYLOAD_PREVIEW_LENGTH) {
      return value;
    }
    return value.substring(0, PAYLOAD_PREVIEW_LENGTH) + "[truncated]";
  }
}
