package org.example.natspring.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.github.malczuuu.natspring.core.NatsOperations;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.example.natspring.telemetry.core.model.ContentModel;
import org.example.natspring.telemetry.core.model.DeadLetterModel;
import org.example.natspring.telemetry.core.model.DeviceEventModel;
import org.example.natspring.telemetry.core.model.DeviceInfoModel;
import org.example.natspring.telemetry.mongodb.DeadLetterDocument;
import org.example.natspring.telemetry.mongodb.DeadLetterRepository;
import org.example.natspring.telemetry.mongodb.DeviceEventDocument;
import org.example.natspring.telemetry.mongodb.DeviceEventRepository;
import org.example.natspring.telemetry.mongodb.DeviceInfoDocument;
import org.example.natspring.telemetry.mongodb.DeviceInfoRepository;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class TelemetryExampleTests {

  @Container @ServiceConnection
  static final NatsContainer nats = new NatsContainer("nats:2.14").withJetStream();

  @Container @ServiceConnection
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.3");

  @Autowired NatsOperations natsClient;
  @Autowired RestTestClient restClient;
  @Autowired DeviceEventRepository deviceEventRepository;
  @Autowired DeviceInfoRepository deviceInfoRepository;
  @Autowired DeadLetterRepository deadLetterRepository;

  private String deviceId;

  @BeforeEach
  void beforeEach() {
    deviceId = "test-device-" + UUID.randomUUID();
    deviceEventRepository.deleteAll();
    deviceInfoRepository.deleteAll();
    deadLetterRepository.deleteAll();
  }

  @Test
  void givenValidMessage_whenPublishedToRaw_thenEventPersistedInDatabase() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(
            deviceId, "temperature", Map.of("value", 23.5, "unit", "Celsius"), Instant.now()));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> !deviceEventRepository.findByDeviceId(deviceId).isEmpty());

    List<DeviceEventDocument> events = deviceEventRepository.findByDeviceId(deviceId);
    assertThat(events).hasSize(1);
    assertThat(events.getFirst().getDeviceId()).isEqualTo(deviceId);
    assertThat(events.getFirst().getType()).isEqualTo("temperature");
    assertThat(events.getFirst().getEventId()).matches("[0-9a-f]{8}-\\d{12}");
  }

  @Test
  void givenValidMessage_whenPublishedToRaw_thenDeviceMetadataUpdated() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(deviceId, "humidity", Map.of("value", 65.0), Instant.now()));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Optional<DeviceInfoDocument> metadata = deviceInfoRepository.findByDeviceId(deviceId);
              assertThat(metadata).isPresent();
              assertThat(metadata.get().getTotalEvents()).isEqualTo(1L);
              assertThat(metadata.get().getLastActivityAt()).isNotNull();
            });

    DeviceInfoDocument metadata = deviceInfoRepository.findByDeviceId(deviceId).orElseThrow();
    assertThat(metadata.getTotalEvents()).isEqualTo(1L);
    assertThat(metadata.getLastActivityAt()).isNotNull();
  }

  @Test
  void givenMultipleValidMessages_whenPublished_thenTotalEventsAccumulates() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(deviceId, "temperature", Map.of("value", 22.0), Instant.now()));
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(deviceId, "pressure", Map.of("value", 1013.0), Instant.now()));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Optional<DeviceInfoDocument> metadata = deviceInfoRepository.findByDeviceId(deviceId);
              assertThat(metadata).isPresent();
              assertThat(metadata.get().getTotalEvents()).isEqualTo(2L);
              assertThat(deviceEventRepository.findByDeviceId(deviceId)).hasSize(2);
            });
  }

  @Test
  void givenValidMessage_whenPublished_thenDeviceEndpointReturnsMetadata() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(deviceId, "temperature", Map.of("value", 20.0), Instant.now()));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> {
              DeviceInfoDocument doc = deviceInfoRepository.findByDeviceId(deviceId).orElse(null);
              return doc != null && doc.getTotalEvents() >= 1;
            });

    restClient
        .get()
        .uri("/api/v1/devices/{id}", deviceId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DeviceInfoModel.class)
        .value(
            response -> {
              assertThat(response.id()).isEqualTo(deviceId);
              assertThat(response.totalEvents()).isEqualTo(1L);
              assertThat(response.lastActivityAt()).isNotNull();
            });
  }

  @Test
  void givenValidMessage_whenPublished_thenHistoryEndpointReturnsEvent() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage(deviceId, "temperature", Map.of("value", 21.5), Instant.now()));

    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> !deviceEventRepository.findByDeviceId(deviceId).isEmpty());

    restClient
        .get()
        .uri("/api/v1/devices/{id}/history-events", deviceId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new ParameterizedTypeReference<ContentModel<DeviceEventModel>>() {})
        .value(
            response -> {
              assertThat(response.content()).hasSize(1);
              assertThat(response.content().getFirst().type()).isEqualTo("temperature");
              assertThat(response.content().getFirst().deviceId()).isEqualTo(deviceId);
              assertThat(response.content().getFirst().id()).matches("[0-9a-f]{8}-\\d{12}");
            });
  }

  @Test
  void givenUnknownDevice_whenDeviceEndpointRequested_thenReturns404() {
    restClient
        .get()
        .uri("/api/v1/devices/{id}", "unknown-device")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void givenUnknownDevice_whenHistoryEndpointRequested_thenReturnsEmptyContent() {
    restClient
        .get()
        .uri("/api/v1/devices/{id}/history-events", "unknown-device")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new ParameterizedTypeReference<ContentModel<DeviceEventModel>>() {})
        .value(response -> assertThat(response.content()).isEmpty());
  }

  @Test
  void givenInvalidMessage_whenPublishedToRaw_thenDeadLettered() {
    natsClient.publish(
        "iot.events.raw",
        new DeviceEventMessage("", "temperature", Map.of("value", 25.0), Instant.now()));

    await().atMost(10, TimeUnit.SECONDS).until(() -> !deadLetterRepository.findAll().isEmpty());

    DeadLetterDocument deadLetter = deadLetterRepository.findAll().getFirst();
    assertThat(deadLetter.getStreamId()).matches("[0-9a-f]{8}-\\d{12}");
    assertThat(deadLetter.getOriginalSubject()).isEqualTo("iot.events.raw");
    assertThat(deadLetter.getReason()).contains("id");
    assertThat(deadLetter.getOriginalStream()).isEqualTo("IOT_RAW");
  }

  @Test
  void givenDeadLetteredMessage_whenListRequested_thenPayloadTruncatedTo32Chars() {
    String longPayload = "x".repeat(100);
    natsClient.publish("iot.events.deadletter", longPayload);

    await().atMost(10, TimeUnit.SECONDS).until(() -> !deadLetterRepository.findAll().isEmpty());

    restClient
        .get()
        .uri("/api/v1/management/dead-letter-messages")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new ParameterizedTypeReference<ContentModel<DeadLetterModel>>() {})
        .value(
            response -> {
              assertThat(response.content()).hasSize(1);
              assertThat(response.content().getFirst().rawPayload())
                  .hasSize(32 + "[truncated]".length())
                  .endsWith("[truncated]");
            });
  }

  @Test
  void givenDeadLetteredMessage_whenDetailRequested_thenFullPayloadReturned() {
    String longPayload = "x".repeat(100);
    natsClient.publish("iot.events.deadletter", longPayload);

    await().atMost(10, TimeUnit.SECONDS).until(() -> !deadLetterRepository.findAll().isEmpty());

    String streamId = deadLetterRepository.findAll().getFirst().getStreamId();

    restClient
        .get()
        .uri("/api/v1/management/dead-letter-messages/{id}", streamId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DeadLetterModel.class)
        .value(
            detail -> {
              assertThat(detail.id()).isEqualTo(streamId);
              assertThat(detail.rawPayload()).hasSize(100);
            });
  }

  @Test
  void givenUnknownDeadLetter_whenDetailRequested_thenReturns404() {
    restClient
        .get()
        .uri("/api/v1/management/dead-letter-messages/{id}", "000000000000000000000000")
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}
