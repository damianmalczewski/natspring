package org.example.natspring.jetstream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.github.malczuuu.natspring.core.NatsClient;
import java.util.Arrays;
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
class JetStreamListenerExampleTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14").withJetStream();

  @Autowired JetStreamListenerExample application;
  @Autowired NatsClient natsClient;
  @Autowired RestTestClient restClient;

  @BeforeEach
  void beforeEach() {
    application.clear();
  }

  @Test
  void orderedConsumerReceivesMeasurementAndEndpointExposesIt() {
    natsClient.publish(
        "telemetry.temperature",
        new SenmlRecord("sensor-single", 1700000000.0, "temperature", 23.5, "Cel"));

    await().atMost(10, TimeUnit.SECONDS).until(() -> !measurementsFor("sensor-single").isEmpty());

    restClient
        .get()
        .uri("/telemetry")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(SenmlRecord[].class)
        .value(
            all -> {
              List<SenmlRecord> received = measurementsFor("sensor-single", all);
              assertThat(received).hasSize(1);
              assertThat(received.getFirst().n()).isEqualTo("temperature");
              assertThat(received.getFirst().v()).isEqualTo(23.5);
              assertThat(received.getFirst().u()).isEqualTo("Cel");
            });
  }

  @Test
  void multipleSubjectsWithinStreamAreAllReceived() {
    natsClient.publish(
        "telemetry.humidity",
        new SenmlRecord("sensor-multi", 1700000001.0, "humidity", 65.0, "%RH"));
    natsClient.publish(
        "telemetry.pressure",
        new SenmlRecord("sensor-multi", 1700000002.0, "pressure", 1013.25, "hPa"));

    await().atMost(10, TimeUnit.SECONDS).until(() -> measurementsFor("sensor-multi").size() >= 2);

    restClient
        .get()
        .uri("/telemetry")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(SenmlRecord[].class)
        .value(
            all -> {
              List<SenmlRecord> received = measurementsFor("sensor-multi", all);
              assertThat(received).hasSize(2);
              assertThat(received.get(0).n()).isEqualTo("humidity");
              assertThat(received.get(1).n()).isEqualTo("pressure");
            });
  }

  private List<SenmlRecord> measurementsFor(String bn) {
    SenmlRecord[] all =
        restClient
            .get()
            .uri("/telemetry")
            .exchange()
            .expectBody(SenmlRecord[].class)
            .returnResult()
            .getResponseBody();
    return measurementsFor(bn, all);
  }

  private static List<SenmlRecord> measurementsFor(String bn, SenmlRecord[] all) {
    if (all == null) return List.of();
    return Arrays.stream(all).filter(m -> bn.equals(m.bn())).toList();
  }
}
