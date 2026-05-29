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

package org.example.natspring.telemetry.rest;

import org.example.natspring.telemetry.core.DeadLetterService;
import org.example.natspring.telemetry.core.model.ContentModel;
import org.example.natspring.telemetry.core.model.DeadLetterModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/management/dead-letter-messages")
public class ManagementController {

  private final DeadLetterService deadLetterService;

  public ManagementController(DeadLetterService deadLetterService) {
    this.deadLetterService = deadLetterService;
  }

  @GetMapping
  public ContentModel<DeadLetterModel> listDeadLetters() {
    return new ContentModel<>(deadLetterService.findAll());
  }

  @GetMapping("/{id}")
  public DeadLetterModel getDeadLetter(@PathVariable String id) {
    return deadLetterService
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
