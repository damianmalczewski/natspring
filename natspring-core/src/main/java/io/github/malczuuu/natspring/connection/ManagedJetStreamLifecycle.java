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

package io.github.malczuuu.natspring.connection;

import io.github.malczuuu.natspring.core.NatsIntegrationException;
import io.github.malczuuu.natspring.core.StreamConfigureException;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JetStreamLifecycle} that auto-creates JetStream streams on startup using the provided
 * {@link StreamConfiguration} list.
 *
 * @since 0.1.0
 */
public class ManagedJetStreamLifecycle implements JetStreamLifecycle {

  private static final Logger log = LoggerFactory.getLogger(ManagedJetStreamLifecycle.class);

  private static final int NOT_FOUND_CODE = 404;
  private static final int STREAM_NOT_FOUND_ERROR = 10059;

  private final Connection connection;
  private final List<StreamConfiguration> streamConfigurations;
  private final boolean enabled;

  private boolean running = false;

  /**
   * Creates a new {@link ManagedJetStreamLifecycle}.
   *
   * @param connection NATS connection used for stream management
   * @param streamConfigurations stream configurations to create on startup
   * @param enabled whether stream auto-creation is enabled
   */
  public ManagedJetStreamLifecycle(
      Connection connection, List<StreamConfiguration> streamConfigurations, boolean enabled) {
    this.connection = connection;
    this.streamConfigurations = streamConfigurations;
    this.enabled = enabled;
  }

  /** Creates all configured JetStream streams that do not yet exist. */
  @Override
  public void start() {
    if (!enabled) {
      log.info("Auto-creation of NATS JetStream streams is disabled, skipping");
      running = true;
      return;
    }

    if (streamConfigurations.isEmpty()) {
      log.info("No NATS stream configurations found, skipping JetStream stream auto-creation");
      running = true;
      return;
    }

    try {
      JetStreamManagement management = connection.jetStreamManagement();
      for (StreamConfiguration stream : streamConfigurations) {
        try {
          StreamInfo info = management.getStreamInfo(stream.getName());

          if (info != null) {
            log.info("JetStream stream={} already exists, skipping", stream.getName());
            continue;
          }
        } catch (JetStreamApiException e) {
          if (!isStreamNotFoundError(e)) {
            throw wrapJetStreamApiException(e);
          }
        }

        try {
          management.addStream(stream);
          log.info("Created JetStream stream={}", stream.getName());
        } catch (Exception e) {
          log.error("Failed to create JetStream stream={}", stream.getName(), e);
          throw e;
        }
      }
    } catch (Exception e) {
      if (e instanceof NatsIntegrationException ex) {
        throw ex;
      }
      if (e instanceof JetStreamApiException ex) {
        throw wrapJetStreamApiException(ex);
      }
      throw new StreamConfigureException("Failed to configure JetStream streams", e);
    }
    running = true;
  }

  /** Marks this manager as stopped. */
  @Override
  public void stop() {
    running = false;
  }

  /**
   * Returns {@code true} if stream provisioning has completed.
   *
   * @return {@code true} if running
   */
  @Override
  public boolean isRunning() {
    return running;
  }

  private boolean isStreamNotFoundError(JetStreamApiException e) {
    return e.getErrorCode() == NOT_FOUND_CODE && e.getApiErrorCode() == STREAM_NOT_FOUND_ERROR;
  }

  private StreamConfigureException wrapJetStreamApiException(JetStreamApiException ex) {
    return new StreamConfigureException(
        "Unable to configure JetStream due to errorCode="
            + ex.getErrorCode()
            + ", errorDescription="
            + ex.getErrorDescription()
            + ", apiErrorCode="
            + ex.getApiErrorCode(),
        ex);
  }
}
