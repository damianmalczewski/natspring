package org.example.natspring.telemetry.nats;

import io.github.malczuuu.natspring.annotation.NatsListener;
import org.example.natspring.telemetry.core.DeviceInfoService;
import org.example.natspring.telemetry.core.model.RecordActivityCommand;
import org.example.natspring.telemetry.nats.model.DeviceEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceActivityListener {

  private static final Logger log = LoggerFactory.getLogger(DeviceActivityListener.class);

  private final DeviceInfoService deviceInfoService;

  public DeviceActivityListener(DeviceInfoService deviceInfoService) {
    this.deviceInfoService = deviceInfoService;
  }

  @NatsListener(subject = "${app.nats.listeners.device-activity.subject}")
  public void onProcessedEvent(DeviceEventMessage message) {
    deviceInfoService.recordActivity(new RecordActivityCommand(message.deviceId()));
    log.info("Updated last activity for id={}", message.deviceId());
  }
}
