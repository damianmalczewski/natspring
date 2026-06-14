package org.example.natspring.listenerdeadletter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.github.malczuuu.natspring.core.NatsOperations;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
class ListenerDeadLetterExampleTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14");

  @Autowired ListenerDeadLetterExample application;
  @Autowired NatsOperations natsClient;
  @Autowired RestTestClient restClient;

  @BeforeEach
  void beforeEach() {
    application.clear();
  }

  @Test
  void whenHandlerThrows_thenMessageIsDeadLettered() {
    natsClient.publish(
        "telemetry.temperature",
        new SenmlRecord("sensor-dlq", 1700000000.0, "temperature", 23.5, "Cel"));

    await().atMost(10, TimeUnit.SECONDS).until(() -> !application.getDeadLetters().isEmpty());

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
            });
  }

  @Test
  void whenMultipleMessagesPublished_thenAllAreDeadLettered() {
    natsClient.publish(
        "telemetry.temperature",
        new SenmlRecord("sensor-multi", 1700000001.0, "temperature", 21.0, "Cel"));
    natsClient.publish(
        "telemetry.humidity",
        new SenmlRecord("sensor-multi", 1700000002.0, "humidity", 60.0, "%RH"));

    await().atMost(10, TimeUnit.SECONDS).until(() -> application.getDeadLetters().size() >= 2);

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
