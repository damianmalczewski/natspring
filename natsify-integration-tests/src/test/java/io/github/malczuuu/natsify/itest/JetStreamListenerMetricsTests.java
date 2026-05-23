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

package io.github.malczuuu.natsify.itest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.malczuuu.natsify.core.NatsOperations;
import io.github.malczuuu.natsify.itest.infra.JetStreamListenerComponent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = IntegrationTestApplication.class)
class JetStreamListenerMetricsTests extends AbstractIntegrationTests {

  @Autowired private MeterRegistry meterRegistry;
  @Autowired private JetStreamListenerComponent handler;
  @Autowired private NatsOperations natsOperations;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenJetStreamListener_whenMessageReceived_thenReceivedCounterIncrementedByOne() {
    Counter before =
        meterRegistry
            .find("nats.jetstream.messages.received")
            .tag("subject", "js.string")
            .tag("stream", "TEST")
            .counter();
    double countBefore = before != null ? before.count() : 0.0;

    natsOperations.publish("js.string", "jetstream-metrics-received-test");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Counter counter =
                  meterRegistry
                      .find("nats.jetstream.messages.received")
                      .tag("subject", "js.string")
                      .tag("stream", "TEST")
                      .counter();
              assertThat(counter).isNotNull();
              assertThat(Objects.requireNonNull(counter).count()).isEqualTo(countBefore + 1.0);
            });
  }

  @Test
  void givenJetStreamListener_whenMessageProcessedSuccessfully_thenAckedCounterIncrementedByOne() {
    Counter before =
        meterRegistry
            .find("nats.jetstream.messages.acked")
            .tag("subject", "js.string")
            .tag("stream", "TEST")
            .counter();
    double countBefore = before != null ? before.count() : 0.0;

    natsOperations.publish("js.string", "jetstream-metrics-acked-test");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Counter counter =
                  meterRegistry
                      .find("nats.jetstream.messages.acked")
                      .tag("subject", "js.string")
                      .tag("stream", "TEST")
                      .counter();
              assertThat(counter).isNotNull();
              assertThat(Objects.requireNonNull(counter).count()).isEqualTo(countBefore + 1.0);
            });
  }

  @Test
  void givenJetStreamListener_whenMessageProcessed_thenDurationTimerIncrementedByOne() {
    Timer before =
        meterRegistry
            .find("nats.jetstream.messages.duration")
            .tag("subject", "js.string")
            .tag("stream", "TEST")
            .timer();
    long countBefore = before != null ? before.count() : 0L;

    natsOperations.publish("js.string", "jetstream-metrics-duration-test");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Timer timer =
                  meterRegistry
                      .find("nats.jetstream.messages.duration")
                      .tag("subject", "js.string")
                      .tag("stream", "TEST")
                      .timer();
              assertThat(timer).isNotNull();
              assertThat(Objects.requireNonNull(timer).count()).isEqualTo(countBefore + 1L);
            });
  }
}
