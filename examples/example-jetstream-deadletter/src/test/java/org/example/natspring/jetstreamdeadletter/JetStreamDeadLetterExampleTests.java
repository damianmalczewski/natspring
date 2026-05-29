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

package org.example.natspring.jetstreamdeadletter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.github.malczuuu.natspring.core.NatsOperations;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class JetStreamDeadLetterExampleTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14.1").withJetStream();

  @Autowired JetStreamDeadLetterExample application;
  @Autowired NatsOperations natsOperations;
  @Autowired RestTestClient restClient;

  @BeforeEach
  void beforeEach() {
    application.clear();
  }

  @Test
  void whenHandlerThrows_thenMessageIsDeadLettered() {
    natsOperations.publish(
        "telemetry.temperature",
        new SenmlRecord("sensor-dlq", 1700000000.0, "temperature", 23.5, "Cel"));

    await().atMost(Duration.ofSeconds(10)).until(() -> !application.getDeadLetters().isEmpty());

    restClient
        .get()
        .uri("/dead-letters")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DeadLetteredMessage[].class)
        .value(
            all -> {
              assertThat(all).hasSize(1);
              DeadLetteredMessage dlq = all[0];

              assertThat(dlq.body()).contains("sensor-dlq");

              List<String> subject = dlq.headers().get("X-Dead-Letter-Subject");
              assertThat(subject).containsExactly("telemetry.temperature");

              List<String> reason = dlq.headers().get("X-Dead-Letter-Reason");
              assertThat(reason).isNotNull();
              assertThat(reason.getFirst()).contains("IllegalArgumentException");

              List<String> exception = dlq.headers().get("X-Dead-Letter-Exception");
              assertThat(exception).containsExactly(IllegalArgumentException.class.getName());

              List<String> timestamp = dlq.headers().get("X-Dead-Letter-Timestamp");
              assertThat(timestamp).isNotNull().isNotEmpty();

              List<String> stream = dlq.headers().get("X-Dead-Letter-Stream");
              assertThat(stream).containsExactly("TELEMETRY");

              List<String> consumer = dlq.headers().get("X-Dead-Letter-Durable");
              assertThat(consumer).containsExactly("telemetry-dlq-listener");

              List<String> delivery = dlq.headers().get("X-Dead-Letter-Delivery");
              assertThat(delivery).containsExactly("1");
            });
  }

  @Test
  void whenMultipleMessagesPublished_thenAllAreDeadLettered() {
    natsOperations.publish(
        "telemetry.temperature",
        new SenmlRecord("sensor-multi", 1700000001.0, "temperature", 21.0, "Cel"));
    natsOperations.publish(
        "telemetry.humidity",
        new SenmlRecord("sensor-multi", 1700000002.0, "humidity", 60.0, "%RH"));

    await().atMost(Duration.ofSeconds(10)).until(() -> application.getDeadLetters().size() >= 2);

    restClient
        .get()
        .uri("/dead-letters")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DeadLetteredMessage[].class)
        .value(all -> assertThat(all).hasSizeGreaterThanOrEqualTo(2));
  }
}
