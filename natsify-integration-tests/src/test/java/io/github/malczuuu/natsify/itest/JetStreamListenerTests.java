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

package io.github.malczuuu.natsify.itest;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.malczuuu.natsify.core.NatsOperations;
import io.github.malczuuu.natsify.itest.infra.JetStreamListenerComponent;
import io.github.malczuuu.natsify.itest.model.SampleMessage;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = IntegrationTestApplication.class)
class JetStreamListenerTests extends AbstractIntegrationTests {

  @Autowired private JetStreamListenerComponent handler;
  @Autowired private NatsOperations natsOperations;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenStringPayloadSubject_whenMessagePublished_thenHandlerReceivesString() throws Exception {
    natsOperations.publish("js.string", "hello jetstream");

    String received = handler.strings.poll(5, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("hello jetstream");
  }

  @Test
  void givenBytesPayloadSubject_whenMessagePublished_thenHandlerReceivesBytes() throws Exception {
    byte[] body = "raw".getBytes(StandardCharsets.UTF_8);

    natsOperations.publish("js.bytes", body);

    byte[] received = handler.bytes.poll(5, TimeUnit.SECONDS);
    assertThat(received).isEqualTo(body);
  }

  @Test
  void givenMessageParamSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsOperations.publish("js.message", "msg body");

    Message received = handler.messages.poll(5, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(new String(received.getData(), StandardCharsets.UTF_8)).isEqualTo("msg body");
  }

  @Test
  void givenObjectSubject_whenJsonMessagePublished_thenHandlerDeserializesObject()
      throws Exception {
    natsOperations.publish("js.object", new SampleMessage("jet", 99));

    SampleMessage received = handler.objects.poll(5, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.name()).isEqualTo("jet");
    assertThat(received.value()).isEqualTo(99);
  }

  @Test
  void givenQueueGroupSubject_whenMessagePublished_thenHandlerReceivesMessage() throws Exception {
    natsOperations.publish("js.queue", "queued");

    String received = handler.queueGroupMessages.poll(5, TimeUnit.SECONDS);
    assertThat(received).isEqualTo("queued");
  }

  @Test
  void givenGenericListSubject_whenJsonMessagePublished_thenHandlerDeserializesList()
      throws Exception {
    natsOperations.publish(
        "js.generic-list", List.of(new SampleMessage("a", 1), new SampleMessage("b", 2)));

    List<SampleMessage> received = handler.genericLists.poll(5, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received.get(0).name()).isEqualTo("a");
    assertThat(received.get(1).name()).isEqualTo("b");
  }

  @Test
  void givenArraySubject_whenJsonMessagePublished_thenHandlerDeserializesArray() throws Exception {
    natsOperations.publish(
        "js.array", List.of(new SampleMessage("c", 3), new SampleMessage("d", 4)));

    SampleMessage[] received = handler.arrays.poll(5, TimeUnit.SECONDS);
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
    Message msg =
        NatsMessage.builder()
            .subject("js.headers-by-type")
            .headers(headers)
            .data(new byte[0])
            .build();

    natsOperations.publish(msg);

    Headers received = handler.headersValuesByType.poll(5, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.getFirst("X-Type")).isEqualTo("by-type-value");
  }
}
