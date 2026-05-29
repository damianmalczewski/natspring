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

package org.example.natspring.listenerdeadletter;

import io.github.malczuuu.natspring.annotation.NatsListener;
import io.nats.client.Message;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ListenerDeadLetterExample {

  private static final Logger log = LoggerFactory.getLogger(ListenerDeadLetterExample.class);

  private final List<DeadLetteredMessage> deadLetters = new CopyOnWriteArrayList<>();

  @GetMapping("/dead-letters")
  public List<DeadLetteredMessage> getDeadLetters() {
    return List.copyOf(deadLetters);
  }

  @NatsListener(subject = "telemetry.>", deadLetterSubject = "dlq.telemetry")
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
    SpringApplication.run(ListenerDeadLetterExample.class, args);
  }
}
