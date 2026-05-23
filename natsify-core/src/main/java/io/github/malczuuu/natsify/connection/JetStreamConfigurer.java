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

package io.github.malczuuu.natsify.connection;

import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.api.StreamConfiguration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JetStreamConfigurer implements JetStreamManager {

  private static final Logger log = LoggerFactory.getLogger(JetStreamConfigurer.class);

  private final List<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizers;
  private final List<StreamConfiguration> streamConfigurations;

  private boolean running = false;

  public JetStreamConfigurer(
      List<ConnectionOptionsBuilderCustomizer> connectionOptionsBuilderCustomizers,
      List<StreamConfiguration> streamConfigurations) {
    this.connectionOptionsBuilderCustomizers = connectionOptionsBuilderCustomizers;
    this.streamConfigurations = streamConfigurations;
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE - 1;
  }

  @Override
  public void start() {
    if (streamConfigurations.isEmpty()) {
      log.info(
          "No NATS listeners or JetStream listeners found, skipping JetStream stream configuration");
      return;
    }

    Options.Builder builder = Options.builder();
    for (ConnectionOptionsBuilderCustomizer customizer : connectionOptionsBuilderCustomizers) {
      builder = customizer.customize(builder);
    }
    Options options = builder.build();

    try (Connection conn = Nats.connect(options)) {
      JetStreamManagement management = conn.jetStreamManagement();
      for (StreamConfiguration sc : streamConfigurations) {
        try {
          management.addStream(sc);
          log.info("Created JetStream stream {}", sc.getName());
        } catch (Exception e) {
          management.updateStream(sc);
          log.info("Updated JetStream stream {}", sc.getName());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to manage JetStream streams", e);
    }
    running = true;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }
}
