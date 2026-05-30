package org.example.natspring.telemetry.nats;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import org.example.natspring.telemetry.core.DeviceInfoService;
import org.example.natspring.telemetry.core.model.IncrementEventCountCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventCounterListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceEventCounterListener.class);

  private final DeviceInfoService deviceInfoService;

  public DeviceEventCounterListener(DeviceInfoService deviceInfoService) {
    this.deviceInfoService = deviceInfoService;
  }

  @JetStreamListener(
      subject = "${app.nats.listeners.device-event-counter.subject}",
      stream = "${app.nats.listeners.device-event-counter.stream}",
      durable = "${app.nats.listeners.device-event-counter.durable}")
  public void onProcessedEvent(DeviceEventMessage message) {
    deviceInfoService.incrementEventCount(new IncrementEventCountCommand(message.deviceId()));
    log.info("Incremented event count for id={}", message.deviceId());
  }
}
