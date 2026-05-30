package org.example.natspring.telemetry.nats;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import io.github.malczuuu.natspring.annotation.NatsHeader;
import java.time.Instant;
import org.example.natspring.telemetry.core.DeviceEventService;
import org.example.natspring.telemetry.core.model.DeviceEventCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventPersistListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventPersistListener.class);

  private final DeviceEventService deviceEventService;

  public DeviceEventPersistListener(DeviceEventService deviceEventService) {
    this.deviceEventService = deviceEventService;
  }

  @JetStreamListener(
      subject = "${app.nats.listeners.device-event-persist.subject}",
      stream = "${app.nats.listeners.device-event-persist.stream}",
      durable = "${app.nats.listeners.device-event-persist.durable}")
  public void onProcessedEvent(
      DeviceEventMessage message, @NatsHeader("X-Event-Id") @Nullable String eventId) {
    if (eventId == null) {
      throw new IllegalArgumentException("Missing event id");
    }
    deviceEventService.persistEvent(
        new DeviceEventCommand(
            eventId,
            message.deviceId(),
            message.type(),
            message.payload(),
            message.timestamp(),
            Instant.now()));
    log.info("Persisted IoT event eventId={}, id={}", eventId, message.deviceId());
  }
}
