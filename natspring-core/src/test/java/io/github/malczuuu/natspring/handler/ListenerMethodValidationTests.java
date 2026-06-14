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

package io.github.malczuuu.natspring.handler;

import static io.github.malczuuu.natspring.handler.ListenerMethodValidation.validateJetStreamListenerMethod;
import static io.github.malczuuu.natspring.handler.ListenerMethodValidation.validateNatsListenerMethod;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.malczuuu.natspring.annotation.NatsHeader;
import io.github.malczuuu.natspring.annotation.NatsHeaders;
import io.github.malczuuu.natspring.annotation.NatsSubject;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListenerMethodValidationTests {

  @SuppressWarnings("unused")
  static void validNoParams() {}

  @SuppressWarnings("unused")
  static void validNatsHeaderString(@NatsHeader("X-Type") String h) {}

  @SuppressWarnings("unused")
  static void validNatsHeaderList(@NatsHeader("X-Type") List<String> h) {}

  @SuppressWarnings("unused")
  static void validNatsHeaderArray(@NatsHeader("X-Type") String[] h) {}

  @SuppressWarnings("unused")
  static void validNatsSubjectString(@NatsSubject String subject) {}

  @SuppressWarnings("unused")
  static void validNatsHeadersType(@NatsHeaders Headers headers) {}

  @SuppressWarnings("unused")
  static void validNatsHeaderEmptyName(@NatsHeader String h) {}

  @SuppressWarnings("unused")
  static void invalidNatsHeaderWrongType(@NatsHeader("X-Type") int h) {}

  @SuppressWarnings("unused")
  static void invalidNatsHeaderListOfInteger(@NatsHeader("X-Type") List<Integer> h) {}

  @SuppressWarnings({"unused", "rawtypes"})
  static void invalidNatsHeaderRawList(@NatsHeader("X-Type") List h) {}

  @SuppressWarnings("unused")
  static void invalidNatsSubjectWrongType(@NatsSubject int subject) {}

  @SuppressWarnings("unused")
  static void invalidNatsHeadersWrongType(@NatsHeaders String headers) {}

  @SuppressWarnings("unused")
  static void validJetStreamMetaData(NatsJetStreamMetaData metaData) {}

  @SuppressWarnings("unused")
  static void invalidNatsListenerMetaData(NatsJetStreamMetaData metaData) {}

  private static Method method(String name, Class<?>... paramTypes) {
    try {
      return ListenerMethodValidationTests.class.getDeclaredMethod(name, paramTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void givenNoParams_whenValidate_thenNoException() {
    assertThatCode(() -> validateJetStreamListenerMethod(method("validNoParams")))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsHeaderStringParam_whenValidate_thenNoException() {
    assertThatCode(
            () -> validateJetStreamListenerMethod(method("validNatsHeaderString", String.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsHeaderListParam_whenValidate_thenNoException() {
    assertThatCode(() -> validateJetStreamListenerMethod(method("validNatsHeaderList", List.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsHeaderArrayParam_whenValidate_thenNoException() {
    assertThatCode(
            () -> validateJetStreamListenerMethod(method("validNatsHeaderArray", String[].class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsSubjectStringParam_whenValidate_thenNoException() {
    assertThatCode(
            () -> validateJetStreamListenerMethod(method("validNatsSubjectString", String.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsHeadersTypeParam_whenValidate_thenNoException() {
    assertThatCode(
            () -> validateJetStreamListenerMethod(method("validNatsHeadersType", Headers.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenMetaDataParamInJetStreamListener_whenValidate_thenNoException() {
    assertThatCode(
            () ->
                validateJetStreamListenerMethod(
                    method("validJetStreamMetaData", NatsJetStreamMetaData.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenMetaDataParamInNatsListener_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                validateNatsListenerMethod(
                    method("invalidNatsListenerMetaData", NatsJetStreamMetaData.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "NatsJetStreamMetaData is only allowed in @JetStreamListener methods");
  }

  @Test
  void givenNatsHeaderWithEmptyName_whenValidate_thenNoException() {
    assertThatCode(
            () -> validateJetStreamListenerMethod(method("validNatsHeaderEmptyName", String.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void givenNatsHeaderWithWrongType_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () -> validateJetStreamListenerMethod(method("invalidNatsHeaderWrongType", int.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@NatsHeader parameter must be String, String[], or List<String>");
  }

  @Test
  void givenNatsHeaderListOfInteger_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                validateJetStreamListenerMethod(
                    method("invalidNatsHeaderListOfInteger", List.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@NatsHeader List parameter must be List<String>");
  }

  @Test
  void givenNatsHeaderRawList_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () -> validateJetStreamListenerMethod(method("invalidNatsHeaderRawList", List.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@NatsHeader List parameter must be List<String>");
  }

  @Test
  void givenNatsSubjectWithWrongType_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () -> validateJetStreamListenerMethod(method("invalidNatsSubjectWrongType", int.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("@NatsSubject parameter must be String");
  }

  @Test
  void givenNatsHeadersWithWrongType_whenValidate_thenThrowsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                validateJetStreamListenerMethod(
                    method("invalidNatsHeadersWrongType", String.class)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "@NatsHeaders parameter must be assignable from io.nats.client.impl.Headers");
  }
}
