package org.example.natspring.telemetry.nats;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import io.github.malczuuu.natspring.core.NatsOperations;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventEntrypointListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventEntrypointListener.class);

  private final NatsOperations natsClient;
  private final StreamSequenceSupport streamSequence;
  private final String processedSubject;

  public DeviceEventEntrypointListener(
      NatsOperations natsClient,
      StreamSequenceSupport streamSequence,
      @Value("${app.nats.listeners.device-event-entrypoint.publish-subject}")
          String processedSubject) {
    this.natsClient = natsClient;
    this.streamSequence = streamSequence;
    this.processedSubject = processedSubject;
  }

  @JetStreamListener(
      subject = "${app.nats.listeners.device-event-entrypoint.subject}",
      stream = "${app.nats.listeners.device-event-entrypoint.stream}",
      durable = "${app.nats.listeners.device-event-entrypoint.durable}",
      deadLetterDeliveries = 5,
      deadLetterSubject = "${app.nats.listeners.device-event-entrypoint.dead-letter-subject}")
  public void onRawEvent(DeviceEventMessage message, NatsJetStreamMetaData meta) {
    validate(message);
    Headers headers = new Headers();
    headers.add("X-Event-Id", streamSequence.build(meta.getStream(), meta.streamSequence()));
    natsClient.publish(processedSubject, headers, message);
    log.info("Processed IoT event id={}, type={}", message.deviceId(), message.type());
  }

  private static void validate(DeviceEventMessage message) {
    if (message.deviceId() == null || message.deviceId().isBlank()) {
      throw new IllegalArgumentException("id must not be blank");
    }
    if (message.type() == null || message.type().isBlank()) {
      throw new IllegalArgumentException("type must not be blank");
    }
    if (message.timestamp() == null) {
      throw new IllegalArgumentException("timestamp must not be null");
    }
  }
}
