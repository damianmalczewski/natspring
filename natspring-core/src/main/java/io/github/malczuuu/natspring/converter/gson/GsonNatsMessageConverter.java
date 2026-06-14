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

package io.github.malczuuu.natspring.converter.gson;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import io.github.malczuuu.natspring.converter.NatsMessageConverter;
import io.github.malczuuu.natspring.core.NatsMessageConversionException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Gson-based {@link NatsMessageConverter} implementation.
 *
 * @since 0.4.0
 */
public class GsonNatsMessageConverter implements NatsMessageConverter {

  private final Gson gson;

  /** Creates a new {@code GsonNatsMessageConverter} with a default {@link Gson} instance. */
  public GsonNatsMessageConverter() {
    this(new Gson());
  }

  /**
   * Creates a new {@code GsonNatsMessageConverter} backed by the given {@link Gson} instance.
   *
   * @param gson the Gson instance to use for serialization and deserialization
   */
  public GsonNatsMessageConverter(Gson gson) {
    this.gson = gson;
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
      return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
    } catch (JsonIOException e) {
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
      return gson.fromJson(new String(data, StandardCharsets.UTF_8), type);
    } catch (JsonParseException e) {
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
      return gson.fromJson(new String(data, StandardCharsets.UTF_8), typeReference.getType());
    } catch (JsonParseException e) {
      throw new NatsMessageConversionException(e);
    }
  }
}
