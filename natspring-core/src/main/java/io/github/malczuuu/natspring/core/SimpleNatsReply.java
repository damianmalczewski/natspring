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

package io.github.malczuuu.natspring.core;

import io.nats.client.Message;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class SimpleNatsReply implements NatsReply {

  private final Message message;
  private final JsonMapper jsonMapper;

  SimpleNatsReply(Message message, JsonMapper jsonMapper) {
    this.message = message;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public Message getMessage() {
    return message;
  }

  @Override
  public <T> T bodyAs(Class<T> type) {
    return jsonMapper.readValue(message.getData(), type);
  }

  @Override
  public <T> T bodyAs(TypeReference<T> typeReference) {
    return jsonMapper.readValue(message.getData(), typeReference);
  }
}
