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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.malczuuu.natspring.converter.gson.GsonNatsMessageConverter;
import io.github.malczuuu.natspring.core.NatsMessageConversionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

class GsonNatsMessageConverterTests {

  private GsonNatsMessageConverter converter;

  @BeforeEach
  void beforeEach() {
    converter = new GsonNatsMessageConverter(new Gson());
  }

  @Test
  void givenObject_whenToBytes_thenProducesJsonBytes() {
    byte[] result = converter.toBytes(new SamplePayload("hello", 42));

    assertThat(new String(result, StandardCharsets.UTF_8))
        .isEqualTo("{\"name\":\"hello\",\"value\":42}");
  }

  @Test
  void givenInvalidObject_whenToBytes_thenThrows() {
    Gson failingGson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(
                SamplePayload.class,
                new TypeAdapter<SamplePayload>() {
                  @Override
                  public void write(JsonWriter out, SamplePayload value) throws IOException {
                    throw new IOException("forced failure");
                  }

                  @Override
                  public SamplePayload read(JsonReader in) {
                    throw new UnsupportedOperationException();
                  }
                })
            .create();
    GsonNatsMessageConverter failingConverter = new GsonNatsMessageConverter(failingGson);

    assertThatThrownBy(() -> failingConverter.toBytes(new SamplePayload("x", 1)))
        .isInstanceOf(NatsMessageConversionException.class);
  }

  @Test
  void givenJsonBytes_whenFromBytesClass_thenDeserializesCorrectly() {
    byte[] data = "{\"name\":\"hello\",\"value\":42}".getBytes(StandardCharsets.UTF_8);

    SamplePayload result = converter.fromBytes(data, SamplePayload.class);

    assertThat(result.name()).isEqualTo("hello");
    assertThat(result.value()).isEqualTo(42);
  }

  @Test
  void givenJsonArrayBytes_whenFromBytesParameterizedTypeReference_thenDeserializesCorrectly() {
    byte[] data = "[\"a\",\"b\",\"c\"]".getBytes(StandardCharsets.UTF_8);

    List<String> result = converter.fromBytes(data, new ParameterizedTypeReference<>() {});

    assertThat(result).containsExactly("a", "b", "c");
  }

  @Test
  void givenInvalidJson_whenFromBytesClass_thenThrows() {
    byte[] data = "not-valid-json".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> converter.fromBytes(data, SamplePayload.class))
        .isInstanceOf(NatsMessageConversionException.class);
  }

  @Test
  void givenNoArgsConstructor_whenCreated_thenCanSerializeAndDeserialize() {
    GsonNatsMessageConverter defaultConverter = new GsonNatsMessageConverter();
    byte[] data = "\"hello\"".getBytes(StandardCharsets.UTF_8);

    String result = defaultConverter.fromBytes(data, String.class);

    assertThat(result).isEqualTo("hello");
  }

  record SamplePayload(String name, int value) {}
}
