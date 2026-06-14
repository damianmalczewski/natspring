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
import io.github.malczuuu.natspring.itest.entrypoint.JetStreamListenerComponent;
import io.github.malczuuu.natspring.itest.entrypoint.SampleMessage;
import io.github.malczuuu.natspring.itest.fixture.AbstractSpringBootTests;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JetStreamListenerTests extends AbstractSpringBootTests {

  @Autowired private JetStreamListenerComponent handler;
  @Autowired private NatsOperations natsClient;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenStringPayloadSubject_whenMessagePublished_thenHandlerReceivesString() throws Exception {
    natsClient.publish("js.string", "hello jetstream");

    String received = handler.strings.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("hello jetstream");
  }

  @Test
  void givenBytesPayloadSubject_whenMessagePublished_thenHandlerReceivesBytes() throws Exception {
    byte[] body = "raw".getBytes(StandardCharsets.UTF_8);

    natsClient.publish("js.bytes", body);

    byte[] received = handler.bytes.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo(body);
  }

  @Test
  void givenMessageParamSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsClient.publish("js.message", "msg body");

    Message received = handler.messages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(new String(received.getData(), StandardCharsets.UTF_8)).isEqualTo("msg body");
  }

  @Test
  void givenObjectSubject_whenJsonMessagePublished_thenHandlerDeserializesObject()
      throws Exception {
    natsClient.publish("js.object", new SampleMessage("jet", 99));

    SampleMessage received = handler.objects.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.name()).isEqualTo("jet");
    assertThat(received.value()).isEqualTo(99);
  }

  @Test
  void givenQueueGroupSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsClient.publish("js.queue", "queued");

    String received = handler.queueGroupMessages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("queued");
  }

  @Test
  void givenPushQueueGroupSubject_whenMessagePublished_thenHandlerReceivesMessage()
      throws Exception {
    natsClient.publish("js.push-queue", "push-queued");

    String received = handler.pushQueueGroupMessages.poll(10, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("push-queued");
  }

  @Test
  void givenGenericListSubject_whenJsonMessagePublished_thenHandlerDeserializesList()
      throws Exception {
    natsClient.publish(
        "js.generic-list", List.of(new SampleMessage("a", 1), new SampleMessage("b", 2)));

    List<SampleMessage> received = handler.genericLists.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received.get(0).name()).isEqualTo("a");
    assertThat(received.get(1).name()).isEqualTo("b");
  }

  @Test
  void givenArraySubject_whenJsonMessagePublished_thenHandlerDeserializesArray() throws Exception {
    natsClient.publish("js.array", List.of(new SampleMessage("c", 3), new SampleMessage("d", 4)));

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
            .subject("js.headers-by-type")
            .headers(headers)
            .data(new byte[0])
            .build();

    natsClient.publish(message);

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("by-type-value");
  }

  @Test
  void givenHeadersByTypeSubject_whenPublishWithBytesAndHeaders_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Type", "bytes-header-value");

    natsClient.publish("js.headers-by-type", headers, new byte[0]);

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("bytes-header-value");
  }

  @Test
  void givenHeadersByTypeSubject_whenPublishWithStringAndHeaders_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Type", "string-header-value");

    natsClient.publish("js.headers-by-type", headers, "");

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("string-header-value");
  }

  @Test
  void givenHeadersByTypeSubject_whenPublishWithObjectAndHeaders_thenHandlerReceivesHeaders()
      throws Exception {
    Headers headers = new Headers();
    headers.add("X-Type", "object-header-value");

    natsClient.publish("js.headers-by-type", headers, new SampleMessage("obj", 1));

    Headers received = handler.headersValuesByType.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("object-header-value");
  }

  @Test
  void givenMetaDataParamSubject_whenMessagePublished_thenHandlerReceivesMetaData()
      throws Exception {
    natsClient.publish("js.metadata", "meta body");

    NatsJetStreamMetaData received = handler.metaDataValues.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getStream()).isEqualTo("TEST");
  }

  @Test
  void givenDlqConfigured_whenHandlerAlwaysFails_thenMessageDeadLettered() throws Exception {
    Headers sourceHeaders = new Headers();
    sourceHeaders.add("X-Trace-Id", "trace-abc-123");
    sourceHeaders.add("X-Source-System", "order-service");

    natsClient.publish("js.dlq-source", sourceHeaders, "dlq test payload");

    Message dlqMessage = handler.deadLetterMessages.poll(10, TimeUnit.SECONDS);
    assertThat(dlqMessage).isNotNull();
    assertThat(new String(dlqMessage.getData(), StandardCharsets.UTF_8))
        .isEqualTo("dlq test payload");
    assertThat(dlqMessage.getHeaders().getFirst("X-Trace-Id")).isEqualTo("trace-abc-123");
    assertThat(dlqMessage.getHeaders().getFirst("X-Source-System")).isEqualTo("order-service");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Subject"))
        .isEqualTo("js.dlq-source");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Stream")).isEqualTo("TEST");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Reason"))
        .contains("RuntimeException");
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Exception"))
        .isEqualTo(RuntimeException.class.getName());
    assertThat(dlqMessage.getHeaders().getFirst("X-Dead-Letter-Timestamp")).isNotNull();
  }
}
