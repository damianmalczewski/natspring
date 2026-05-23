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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.malczuuu.natsify.SampleMessage;
import io.github.malczuuu.natsify.annotation.NatsHeader;
import io.github.malczuuu.natsify.annotation.NatsHeaders;
import io.github.malczuuu.natsify.annotation.NatsPayload;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

class SimpleMessageArgumentResolverTests {

  private MessageArgumentResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new SimpleMessageArgumentResolver(JsonMapper.builder().findAndAddModules().build());
  }

  @Test
  void givenMessageParam_whenResolved_thenReturnsMessageAsIs() {
    Message msg = message("body".getBytes(StandardCharsets.UTF_8));

    Object result = resolver.resolveArgument(param("withMessage", Message.class), msg);

    assertThat(result).isSameAs(msg);
  }

  @Test
  void givenBytesParam_whenResolved_thenReturnsRawData() {
    byte[] body = {1, 2, 3};

    Object result = resolver.resolveArgument(param("withBytes", byte[].class), message(body));

    assertThat(result).isEqualTo(body);
  }

  @Test
  void givenStringParam_whenResolved_thenReturnsUtf8Decoded() {
    byte[] body = "hello".getBytes(StandardCharsets.UTF_8);

    Object result = resolver.resolveArgument(param("withString", String.class), message(body));

    assertThat(result).isEqualTo("hello");
  }

  @Test
  void givenStringParamWithNullData_whenResolved_thenReturnsNull() {
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn(null);

    Object result = resolver.resolveArgument(param("withString", String.class), msg);

    assertThat(result).isNull();
  }

  @Test
  void givenNatsHeaderValueAttribute_whenResolved_thenResolvesHeaderByName() {
    Headers headers = new Headers();
    headers.add("X-Foo", "bar");

    Object result =
        resolver.resolveArgument(
            param("withHeaderByValue", String.class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isEqualTo("bar");
  }

  @Test
  void givenNatsHeaderNameAttribute_whenResolved_thenResolvesHeaderByName() {
    Headers headers = new Headers();
    headers.add("X-Foo", "baz");

    Object result =
        resolver.resolveArgument(
            param("withHeaderByName", String.class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isEqualTo("baz");
  }

  @Test
  void givenNatsHeaderWithNoMessageHeaders_whenResolved_thenReturnsNull() {
    Object result =
        resolver.resolveArgument(param("withHeaderByValue", String.class), message(new byte[0]));

    assertThat(result).isNull();
  }

  @NullUnmarked
  @Test
  void givenMultiValuedHeader_whenResolvedAsList_thenReturnsAllValues() {
    Headers headers = new Headers();
    headers.add("X-Foo", "a");
    headers.add("X-Foo", "b");

    Object result =
        resolver.resolveArgument(
            param("withHeaderAsList", List.class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isInstanceOf(List.class);
    @SuppressWarnings("unchecked")
    List<String> headerList = (List<String>) result;
    assertThat(headerList).containsExactly("a", "b");
  }

  @NullUnmarked
  @Test
  void givenMultiValuedHeader_whenResolvedAsArray_thenReturnsAllValues() {
    Headers headers = new Headers();
    headers.add("X-Foo", "x");
    headers.add("X-Foo", "y");

    Object result =
        resolver.resolveArgument(
            param("withHeaderAsArray", String[].class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isInstanceOf(String[].class);
    assertThat((String[]) result).containsExactly("x", "y");
  }

  @NullUnmarked
  @Test
  void givenNatsHeadersAnnotation_whenResolved_thenReturnsAllHeaders() {
    Headers headers = new Headers();
    headers.add("X-A", "1");

    Object result =
        resolver.resolveArgument(
            param("withNatsHeaders", Headers.class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isInstanceOf(Headers.class);
    assertThat(((Headers) result).getFirst("X-A")).isEqualTo("1");
  }

  @NullUnmarked
  @Test
  void givenHeadersTypeWithoutAnnotation_whenResolved_thenReturnsAllHeaders() {
    Headers headers = new Headers();
    headers.add("X-B", "2");

    Object result =
        resolver.resolveArgument(
            param("withHeadersByType", Headers.class), messageWithHeaders(new byte[0], headers));

    assertThat(result).isInstanceOf(Headers.class);
    assertThat(((Headers) result).getFirst("X-B")).isEqualTo("2");
  }

  @Test
  void givenNatsPayloadAnnotationOnHeadersType_whenResolved_thenDeserializesData() {
    byte[] json = "[1,2,3]".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(
            () ->
                resolver.resolveArgument(
                    param("withDataAnnotatedHeaders", Headers.class), message(json)))
        .isInstanceOf(JacksonException.class);
  }

  @Test
  void givenObjectParam_whenResolved_thenDeserializesJson() {
    byte[] json = "{\"name\":\"test\",\"value\":42}".getBytes(StandardCharsets.UTF_8);

    Object result =
        resolver.resolveArgument(param("withObject", SampleMessage.class), message(json));

    assertThat(result).isEqualTo(new SampleMessage("test", 42));
  }

  @Test
  void givenObjectParamWithNullData_whenResolved_thenReturnsNull() {
    Message msg = Mockito.mock(Message.class);
    Mockito.when(msg.getData()).thenReturn(null);

    Object result = resolver.resolveArgument(param("withObject", SampleMessage.class), msg);

    assertThat(result).isNull();
  }

  @NullUnmarked
  @Test
  void givenGenericListParam_whenResolved_thenPreservesTypeAndDeserializes() throws Exception {
    byte[] json =
        "[{\"name\":\"a\",\"value\":1},{\"name\":\"b\",\"value\":2}]"
            .getBytes(StandardCharsets.UTF_8);
    Parameter listParam =
        Methods.class.getDeclaredMethod("withList", List.class).getParameters()[0];

    Object result = resolver.resolveArgument(listParam, message(json));

    assertThat(result).isInstanceOf(List.class);
    @SuppressWarnings("unchecked")
    List<SampleMessage> list = (List<SampleMessage>) result;
    assertThat(list).hasSize(2);
    assertThat(list.get(0)).isEqualTo(new SampleMessage("a", 1));
    assertThat(list.get(1)).isEqualTo(new SampleMessage("b", 2));
  }

  @NullUnmarked
  @Test
  void givenArrayParam_whenResolved_thenDeserializesFromJson() {
    byte[] json =
        "[{\"name\":\"c\",\"value\":3},{\"name\":\"d\",\"value\":4}]"
            .getBytes(StandardCharsets.UTF_8);

    Object result =
        resolver.resolveArgument(param("withArray", SampleMessage[].class), message(json));

    assertThat(result).isInstanceOf(SampleMessage[].class);
    SampleMessage[] arr = (SampleMessage[]) result;
    assertThat(arr).hasSize(2);
    assertThat(arr[0]).isEqualTo(new SampleMessage("c", 3));
    assertThat(arr[1]).isEqualTo(new SampleMessage("d", 4));
  }

  @Test
  void givenInvalidJson_whenResolved_thenThrowsRuntimeException() {
    byte[] bad = "not-json".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(
            () -> resolver.resolveArgument(param("withObject", SampleMessage.class), message(bad)))
        .isInstanceOf(JacksonException.class);
  }

  private static Parameter param(String methodName, Class<?>... paramTypes) {
    try {
      Method m = Methods.class.getDeclaredMethod(methodName, paramTypes);
      return m.getParameters()[0];
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Message message(byte[] data) {
    return NatsMessage.builder().subject("test").data(data).build();
  }

  private static Message messageWithHeaders(byte[] data, Headers headers) {
    return NatsMessage.builder().subject("test").data(data).headers(headers).build();
  }

  @SuppressWarnings("unused")
  private static class Methods {

    void withMessage(Message msg) {}

    void withBytes(byte[] b) {}

    void withString(String s) {}

    void withObject(SampleMessage obj) {}

    void withList(List<SampleMessage> list) {}

    void withArray(SampleMessage[] arr) {}

    void withNatsHeaders(@NatsHeaders Headers h) {}

    void withHeadersByType(Headers h) {}

    void withHeaderByValue(@NatsHeader("X-Foo") String h) {}

    void withHeaderByName(@NatsHeader(name = "X-Foo") String h) {}

    void withHeaderAsList(@NatsHeader("X-Foo") List<String> h) {}

    void withHeaderAsArray(@NatsHeader("X-Foo") String[] h) {}

    void withDataAnnotatedHeaders(@NatsPayload Headers h) {}
  }
}
