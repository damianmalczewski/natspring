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
import static org.awaitility.Awaitility.await;

import io.github.malczuuu.natspring.core.NatsClient;
import io.github.malczuuu.natspring.core.NatsReply;
import io.github.malczuuu.natspring.itest.entrypoint.NatsListenerComponent;
import io.github.malczuuu.natspring.itest.entrypoint.SampleMessage;
import io.github.malczuuu.natspring.itest.fixture.AbstractSpringBootTests;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NatsListenerRequestReplyTests extends AbstractSpringBootTests {

  @Autowired private NatsListenerComponent handler;
  @Autowired private NatsClient natsClient;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenBytesReplyListener_whenRequested_thenRepliesWithSameBytes() throws Exception {
    byte[] payload = "hello bytes".getBytes(StandardCharsets.UTF_8);

    NatsReply reply =
        natsClient.request("rpc.bytes", payload, Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);

    assertThat(reply).isNotNull();
    assertThat(reply.getMessage().getData()).isEqualTo(payload);
  }

  @Test
  void givenStringReplyListener_whenRequested_thenRepliesWithUpperCasedString() throws Exception {
    NatsReply reply =
        natsClient.request("rpc.string", "hello", Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);

    assertThat(reply).isNotNull();
    assertThat(new String(reply.getMessage().getData(), StandardCharsets.UTF_8)).isEqualTo("HELLO");
  }

  @Test
  void givenObjectReplyListener_whenRequested_thenRepliesWithTransformedObject() throws Exception {
    NatsReply reply =
        natsClient
            .request("rpc.object", new SampleMessage("foo", 3), Duration.ofSeconds(5))
            .get(10, TimeUnit.SECONDS);

    assertThat(reply).isNotNull();
    SampleMessage result = reply.bodyAs(SampleMessage.class);
    assertThat(result.name()).isEqualTo("FOO");
    assertThat(result.value()).isEqualTo(6);
  }

  @Test
  void givenMessageReplyListener_whenRequested_thenRepliesWithMessageBody() throws Exception {
    byte[] payload = "ping".getBytes(StandardCharsets.UTF_8);

    NatsReply reply =
        natsClient.request("rpc.message", payload, Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);

    assertThat(reply).isNotNull();
    assertThat(new String(reply.getMessage().getData(), StandardCharsets.UTF_8))
        .isEqualTo("echo:ping");
  }

  @Test
  void givenReplyToParamListener_whenRequested_thenReplyToAddressIsInjected() throws Exception {
    natsClient.request("rpc.reply-to-param", "data", Duration.ofSeconds(5));

    String replyTo = handler.replyToValues.poll(10, TimeUnit.SECONDS);
    assertThat(replyTo).isNotNull().isNotEmpty();
  }

  @Test
  void givenReplyToParamListener_whenPublishedWithoutReplyTo_thenReplyToIsNull() throws Exception {
    natsClient.publish("rpc.reply-to-param", "data");

    String replyTo = handler.replyToValues.poll(10, TimeUnit.SECONDS);
    assertThat(replyTo).isNotNull().isEmpty();
  }

  @Test
  void givenListenerWithReturnValue_whenPublishedWithoutReplyTo_thenReplyDiscardedSilently()
      throws Exception {
    natsClient.publish("rpc.no-reply-to", "test");

    Thread.sleep(500);
  }

  @Test
  void givenMessageReplyListener_whenPublishedWithoutReplyTo_thenHandlerRunsAndReplyIsDiscarded()
      throws Exception {
    natsClient.publish("rpc.message", "ping".getBytes(StandardCharsets.UTF_8));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(handler.rpcMessages.peek()).isNotNull());
  }

  @Test
  void givenRequestMethod_whenStringPayload_thenRepliesCorrectly() throws Exception {
    NatsReply reply =
        natsClient.request("rpc.string", "world", Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);

    assertThat(new String(reply.getMessage().getData(), StandardCharsets.UTF_8)).isEqualTo("WORLD");
  }
}
