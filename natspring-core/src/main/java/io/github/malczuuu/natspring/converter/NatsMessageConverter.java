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

package io.github.malczuuu.natspring.converter;

import org.springframework.core.ParameterizedTypeReference;

/**
 * Strategy for serializing objects to bytes and deserializing bytes back to objects for NATS
 * message payloads.
 *
 * @since 0.3.0
 */
public interface NatsMessageConverter {

  /**
   * Serializes the given object to a byte array.
   *
   * @param object the object to serialize
   * @return serialized bytes
   */
  byte[] toBytes(Object object);

  /**
   * Deserializes a byte array to an instance of the given class.
   *
   * @param data the bytes to deserialize
   * @param type the target class
   * @param <T> the target type
   * @return deserialized instance
   */
  <T> T fromBytes(byte[] data, Class<T> type);

  /**
   * Deserializes a byte array to an instance of the given generic type.
   *
   * @param data the bytes to deserialize
   * @param typeReference the target type reference (may be parameterized)
   * @param <T> the target type
   * @return deserialized instance
   */
  <T> T fromBytes(byte[] data, ParameterizedTypeReference<T> typeReference);
}
