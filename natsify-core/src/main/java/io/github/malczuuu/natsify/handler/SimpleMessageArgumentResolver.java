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
import io.github.malczuuu.natsify.annotation.NatsSubject;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson-based {@link MessageArgumentResolver} that resolves listener method parameters from NATS
 * message data, headers, and metadata.
 *
 * <p>Supports parameters of type {@link Message}, {@link Headers} (with or without {@link
 * NatsHeaders @NatsHeaders}), individual header values via {@link NatsHeader @NatsHeader}, the
 * message subject via {@link NatsSubject @NatsSubject}, {@link NatsJetStreamMetaData}, {@code
 * byte[]}, {@link String}, and arbitrary JSON-deserializable types.
 */
public class SimpleMessageArgumentResolver implements MessageArgumentResolver {

  private final JsonMapper jsonMapper;

  /**
   * Creates a new {@code SimpleMessageArgumentResolver}.
   *
   * @param jsonMapper Jackson mapper used for JSON deserialization of payload parameters
   */
  public SimpleMessageArgumentResolver(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  /**
   * Resolves all parameters for a listener method from the given message.
   *
   * @param parameters the method parameters to resolve
   * @param message the received message
   * @return array of resolved arguments, or {@code null}
   */
  @Override
  public Object @Nullable [] resolveArguments(Parameter[] parameters, Message message) {
    List<@Nullable Object> args = new ArrayList<>(parameters.length);
    for (Parameter param : parameters) {
      args.add(resolveArgument(param, message));
    }
    return args.toArray();
  }

  /**
   * Resolves a single method parameter from the given message.
   *
   * @param parameter the method parameter to resolve
   * @param message the received message
   * @return the resolved argument, or {@code null}
   */
  @Override
  public @Nullable Object resolveArgument(Parameter parameter, Message message) {
    if (Message.class.isAssignableFrom(parameter.getType())) {
      return message;
    }
    NatsHeader natsHeader = parameter.getAnnotation(NatsHeader.class);
    if (natsHeader != null) {
      String name = natsHeader.value().isEmpty() ? natsHeader.name() : natsHeader.value();
      Headers msgHeaders = message.getHeaders();
      if (msgHeaders == null) {
        return null;
      }
      if (List.class.isAssignableFrom(parameter.getType())) {
        return msgHeaders.get(name);
      }
      if (parameter.getType() == String[].class) {
        List<String> values = msgHeaders.get(name);
        return values != null ? values.toArray(new String[0]) : null;
      }
      return msgHeaders.getFirst(name);
    }
    if (parameter.isAnnotationPresent(NatsSubject.class)) {
      return message.getSubject();
    }
    if (parameter.isAnnotationPresent(NatsHeaders.class)
        || (!parameter.isAnnotationPresent(NatsPayload.class)
            && Headers.class.isAssignableFrom(parameter.getType()))) {
      return message.getHeaders() != null ? message.getHeaders() : new Headers(null, false);
    }
    if (!parameter.isAnnotationPresent(NatsPayload.class)
        && NatsJetStreamMetaData.class.isAssignableFrom(parameter.getType())) {
      return message.metaData();
    }
    byte[] data = message.getData();
    if (parameter.getType() == byte[].class) {
      return data;
    }
    if (parameter.getType() == String.class) {
      return data != null ? new String(data, StandardCharsets.UTF_8) : null;
    }
    return data != null
        ? jsonMapper.readValue(data, jsonMapper.constructType(parameter.getParameterizedType()))
        : null;
  }
}
