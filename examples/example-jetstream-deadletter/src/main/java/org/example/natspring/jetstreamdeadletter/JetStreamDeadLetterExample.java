package org.example.natspring.jetstreamdeadletter;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import io.github.malczuuu.natspring.annotation.NatsListener;
import io.nats.client.Message;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.impl.Headers;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class JetStreamDeadLetterExample {

  private static final Logger log = LoggerFactory.getLogger(JetStreamDeadLetterExample.class);

  private final List<DeadLetteredMessage> deadLetters = new CopyOnWriteArrayList<>();

  @Bean
  StreamConfiguration telemetryStreamConfiguration() {
    return StreamConfiguration.builder().name("TELEMETRY").subjects("telemetry.>").build();
  }

  @GetMapping("/dead-letters")
  public List<DeadLetteredMessage> getDeadLetters() {
    return List.copyOf(deadLetters);
  }

  @JetStreamListener(
      subject = "telemetry.>",
      stream = "TELEMETRY",
      durable = "telemetry-dlq-listener",
      deadLetterSubject = "dlq.telemetry",
      deadLetterDeliveries = 1)
  public void onRecord(SenmlRecord record) {
    throw new IllegalArgumentException("simulated handler failure");
  }

  @NatsListener(subject = "dlq.telemetry")
  public void onDeadLetter(Message message) {
    deadLetters.add(capture(message));
    log.info("Received dead-letter on subject={}", message.getSubject());
  }

  public void clear() {
    deadLetters.clear();
  }

  private static DeadLetteredMessage capture(Message message) {
    String body =
        message.getData() != null ? new String(message.getData(), StandardCharsets.UTF_8) : "";
    Map<String, List<String>> headers = new LinkedHashMap<>();
    Headers messageHeaders = message.getHeaders();
    if (messageHeaders != null) {
      for (String key : messageHeaders.keySet()) {
        List<String> values = messageHeaders.get(key);
        if (values != null) {
          headers.put(key, List.copyOf(values));
        }
      }
    }
    return new DeadLetteredMessage(body, headers);
  }

  public static void main(String[] args) {
    SpringApplication.run(JetStreamDeadLetterExample.class, args);
  }
}
