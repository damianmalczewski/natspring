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

package org.example.natspring.jetstream;

import io.github.malczuuu.natspring.annotation.JetStreamListener;
import io.nats.client.api.StreamConfiguration;
import java.util.List;
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
public class JetStreamListenerExample {

  private static final Logger log = LoggerFactory.getLogger(JetStreamListenerExample.class);

  private final List<SenmlRecord> records = new CopyOnWriteArrayList<>();

  @Bean
  StreamConfiguration telemetryStreamConfiguration() {
    return StreamConfiguration.builder().name("TELEMETRY").subjects("telemetry.>").build();
  }

  @GetMapping(path = "/telemetry")
  public List<SenmlRecord> getAll() {
    return List.copyOf(records);
  }

  @JetStreamListener(subject = "telemetry.>", stream = "TELEMETRY", durable = "telemetry-listener")
  public void onRecord(SenmlRecord record) {
    records.add(record);
    log.info("Received telemetry on JetStreamListener; record={}", record);
  }

  public void clear() {
    records.clear();
  }

  public static void main(String[] args) {
    SpringApplication.run(JetStreamListenerExample.class, args);
  }
}
