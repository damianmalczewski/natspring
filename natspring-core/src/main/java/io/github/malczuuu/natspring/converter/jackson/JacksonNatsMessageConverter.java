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

package io.github.malczuuu.natspring.converter.jackson;

import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.github.malczuuu.natspring.core.NatsMessageConversionException;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson-based {@link NatsMessageConverter} implementation.
 *
 * @since 0.3.0
 */
public class JacksonNatsMessageConverter implements NatsMessageConverter {

  private final JsonMapper jsonMapper;

  /** Creates a new {@code JacksonNatsMessageConverter} with a default {@link JsonMapper}. */
  public JacksonNatsMessageConverter() {
    this(JsonMapper.builder().findAndAddModules().build());
  }

  /**
   * Creates a new {@code JacksonNatsMessageConverter} backed by the given {@link JsonMapper}.
   *
   * @param jsonMapper the mapper to use for serialization and deserialization
   */
  public JacksonNatsMessageConverter(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  /**
   * Serializes the given object to a byte array.
   *
   * @param object the object to serialize
   * @return serialized bytes
   * @throws NatsMessageConversionException when unable to serialize object
   */
  @Override
  public byte[] toBytes(Object object) {
    try {
      return jsonMapper.writeValueAsBytes(object);
    } catch (JacksonException e) {
      throw new NatsMessageConversionException(e);
    }
  }

  /**
   * Deserializes a byte array to an instance of the given class.
   *
   * @param data the bytes to deserialize
   * @param type the target class
   * @param <T> the target type
   * @return deserialized instance
   * @throws NatsMessageConversionException when unable to deserialize data
   */
  @Override
  public <T> T fromBytes(byte[] data, Class<T> type) {
    try {
      return jsonMapper.readValue(data, type);
    } catch (JacksonException e) {
      throw new NatsMessageConversionException(e);
    }
  }

  /**
   * Deserializes a byte array to an instance of the given generic type.
   *
   * @param data the bytes to deserialize
   * @param typeReference the target type reference (may be parameterized)
   * @param <T> the target type
   * @return deserialized instance
   * @throws NatsMessageConversionException when unable to deserialize data
   */
  @Override
  public <T> T fromBytes(byte[] data, ParameterizedTypeReference<T> typeReference) {
    try {
      return jsonMapper.readValue(data, jsonMapper.constructType(typeReference.getType()));
    } catch (JacksonException e) {
      throw new NatsMessageConversionException(e);
    }
  }
}
