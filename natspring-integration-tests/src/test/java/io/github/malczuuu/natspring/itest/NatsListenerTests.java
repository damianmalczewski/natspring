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

package io.github.malczuuu.natspring.itest;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.itest.generic.AbstractSpringBootTests;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NatsListenerTests extends AbstractSpringBootTests {

  @Autowired private NatsListenerComponent handler;
  @Autowired private NatsOperations natsOperations;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenNoArgsSubject_whenMessagePublished_thenHandlerInvoked() throws Exception {
    natsOperations.publish("combo.no-args", new byte[0]);

    assertThat(handler.noArgsLatch.await(10, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  void givenMessageParamSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsOperations.publish("combo.message", "hello".getBytes(StandardCharsets.UTF_8));

    Message received = handler.messages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(new String(received.getData(), StandardCharsets.UTF_8)).isEqualTo("hello");
  }

  @Test
  void givenBytesParamSubject_whenMessagePublished_thenHandlerReceivesBytes() throws Exception {
    byte[] body = "raw bytes".getBytes(StandardCharsets.UTF_8);

    natsOperations.publish("combo.bytes", body);

    byte[] received = handler.bytesPayloads.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo(body);
  }

  @Test
  void givenStringParamSubject_whenMessagePublished_thenHandlerReceivesString() throws Exception {
    natsOperations.publish("combo.string", "hello string");

    String received = handler.stringPayloads.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("hello string");
  }

  @Test
  void givenBytesPayloadAnnotationSubject_whenMessagePublished_thenHandlerReceivesBytes()
      throws Exception {
    byte[] body = "payload bytes".getBytes(StandardCharsets.UTF_8);

    natsOperations.publish("combo.bytes-payload", body);

    byte[] received = handler.bytesWithPayloadAnnotation.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo(body);
  }

  @Test
  void givenStringPayloadAnnotationSubject_whenMessagePublished_thenHandlerReceivesString()
      throws Exception {
    natsOperations.publish("combo.string-payload", "payload string");

    String received = handler.stringsWithPayloadAnnotation.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("payload string");
  }

  @Test
  void givenObjectSubject_whenJsonMessagePublished_thenHandlerDeserializesObject()
      throws Exception {
    natsOperations.publish("combo.object", new SampleMessage("test", 42));

    SampleMessage received = handler.objects.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.name()).isEqualTo("test");
    assertThat(received.value()).isEqualTo(42);
  }

  @Test
  void givenHeaderParamSubject_whenMessageWithHeaderPublished_thenHandlerReceivesHeaderValue()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Key", "header-value");
    Message message =
        NatsMessage.builder().subject("combo.header").headers(headers).data(new byte[0]).build();

    natsOperations.publish(message);

    String received = handler.headerValues.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("header-value");
  }

  @Test
  void givenHeadersParamSubject_whenMessageWithHeadersPublished_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Foo", "foo-value");
    headers.add("X-Bar", "bar-value");
    Message message =
        NatsMessage.builder().subject("combo.headers").headers(headers).data(new byte[0]).build();

    natsOperations.publish(message);

    Headers received = handler.headersValues.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Foo")).isEqualTo("foo-value");
    assertThat(received.getFirst("X-Bar")).isEqualTo("bar-value");
  }

  @Test
  void givenQueueGroupSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsOperations.publish("combo.queue-queue", "queued");

    String received = handler.queueGroupMessages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("queued");
  }

  @Test
  void givenPropertyResolvedSubjectAndQueue_whenMessagePublished_thenHandlerReceivesMessage()
      throws Exception {
    natsOperations.publish("combo.from-property", "from property");

    String received = handler.propertySubjectMessages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("from property");
  }

  @Test
  void givenGenericListSubject_whenJsonMessagePublished_thenHandlerDeserializesList()
      throws Exception {
    natsOperations.publish(
        "combo.generic-list", List.of(new SampleMessage("a", 1), new SampleMessage("b", 2)));

    List<SampleMessage> received = handler.genericLists.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received.get(0).name()).isEqualTo("a");
    assertThat(received.get(1).name()).isEqualTo("b");
  }

  @Test
  void givenArraySubject_whenJsonMessagePublished_thenHandlerDeserializesArray() throws Exception {
    natsOperations.publish(
        "combo.array", List.of(new SampleMessage("c", 3), new SampleMessage("d", 4)));

    SampleMessage[] received = handler.arrays.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received[0].name()).isEqualTo("c");
    assertThat(received[1].name()).isEqualTo("d");
  }

  @Test
  void givenHeadersByTypeSubject_whenMessageWithHeaderPublished_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Type", "by-type-value");
    Message message =
        NatsMessage.builder()
            .subject("combo.headers-by-type")
            .headers(headers)
            .data(new byte[0])
            .build();

    natsOperations.publish(message);

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("by-type-value");
  }

  @Test
  void givenHeaderParamSubject_whenPublishWithBytesAndHeaders_thenHandlerReceivesHeaderValue()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Key", "bytes-header-value");

    natsOperations.publish("combo.header", headers, new byte[0]);

    String received = handler.headerValues.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("bytes-header-value");
  }

  @Test
  void givenHeadersParamSubject_whenPublishWithStringAndHeaders_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Foo", "string-foo");
    headers.add("X-Bar", "string-bar");

    natsOperations.publish("combo.headers", headers, "");

    Headers received = handler.headersValues.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Foo")).isEqualTo("string-foo");
    assertThat(received.getFirst("X-Bar")).isEqualTo("string-bar");
  }

  @Test
  void givenHeadersByTypeSubject_whenPublishWithObjectAndHeaders_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Type", "object-header-value");

    natsOperations.publish("combo.headers-by-type", headers, new SampleMessage("obj", 1));

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("object-header-value");
  }

  @Test
  void givenDlqConfigured_whenHandlerAlwaysFails_thenMessageDeadLettered() throws Exception {
    Headers sourceHeaders = new Headers();
    sourceHeaders.add("X-Trace-Id", "trace-abc-123");
    sourceHeaders.add("X-Source-System", "order-service");

    natsOperations.publish("combo.dlq-source", sourceHeaders, "dlq test payload");

    Message dlqMessage = handler.deadLetterMessages.poll(10, TimeUnit.SECONDS);
    assertThat(dlqMessage).isNotNull();
    assertThat(new String(dlqMessage.getData(), StandardCharsets.UTF_8))
        .isEqualTo("dlq test payload");
    assertThat(dlqMessage.getHeaders().getFirst("X-Trace-Id")).isEqualTo("trace-abc-123");
    assertThat(dlqMessage.getHeaders().getFirst("X-Source-System")).isEqualTo("order-service");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Subject"))
        .isEqualTo("combo.dlq-source");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Reason"))
        .contains("RuntimeException");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Exception"))
        .isEqualTo(RuntimeException.class.getName());
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Timestamp")).isNotNull();
  }
}
