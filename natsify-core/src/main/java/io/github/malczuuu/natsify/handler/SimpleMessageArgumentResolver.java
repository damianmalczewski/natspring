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

package io.github.malczuuu.natsify.handler;

import io.github.malczuuu.natsify.annotation.NatsHeader;
import io.github.malczuuu.natsify.annotation.NatsHeaders;
import io.github.malczuuu.natsify.annotation.NatsPayload;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

public class SimpleMessageArgumentResolver implements MessageArgumentResolver {

  private final JsonMapper jsonMapper;

  public SimpleMessageArgumentResolver(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public Object @Nullable [] resolveArguments(Parameter[] params, Message msg) {
    List<@Nullable Object> args = new ArrayList<>(params.length);
    for (Parameter param : params) {
      args.add(resolveArgument(param, msg));
    }
    return args.toArray();
  }

  @Override
  public @Nullable Object resolveArgument(Parameter param, Message msg) {
    if (Message.class.isAssignableFrom(param.getType())) {
      return msg;
    }
    NatsHeader natsHeader = param.getAnnotation(NatsHeader.class);
    if (natsHeader != null) {
      String name = natsHeader.value().isEmpty() ? natsHeader.name() : natsHeader.value();
      Headers msgHeaders = msg.getHeaders();
      if (msgHeaders == null) {
        return null;
      }
      if (List.class.isAssignableFrom(param.getType())) {
        return msgHeaders.get(name);
      }
      if (param.getType() == String[].class) {
        List<String> values = msgHeaders.get(name);
        return values != null ? values.toArray(new String[0]) : null;
      }
      return msgHeaders.getFirst(name);
    }
    if (param.isAnnotationPresent(NatsHeaders.class)
        || (!param.isAnnotationPresent(NatsPayload.class)
            && Headers.class.isAssignableFrom(param.getType()))) {
      return msg.getHeaders();
    }
    byte[] data = msg.getData();
    if (param.getType() == byte[].class) {
      return data;
    }
    if (param.getType() == String.class) {
      return data != null ? new String(data, StandardCharsets.UTF_8) : null;
    }
    return data != null
        ? jsonMapper.readValue(data, jsonMapper.constructType(param.getParameterizedType()))
        : null;
  }
}
