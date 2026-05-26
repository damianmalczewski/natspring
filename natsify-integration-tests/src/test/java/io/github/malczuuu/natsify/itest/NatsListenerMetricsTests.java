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
import io.github.malczuuu.natsify.itest.infra.NatsListenerComponent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Entrypoint.class)
class NatsListenerMetricsTests extends AbstractIntegrationTests {

  @Autowired private MeterRegistry meterRegistry;
  @Autowired private NatsListenerComponent handler;
  @Autowired private NatsOperations natsOperations;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenNatsListener_whenMessageReceived_thenReceivedCounterIncrementedByOne()
      throws Exception {
    Counter before =
        meterRegistry
            .find("nats.listener.messages.received")
            .tag("subject", "combo.string")
            .tag("queue", "")
            .counter();
    double countBefore = before != null ? before.count() : 0.0;

    natsOperations.publish("combo.string", "metrics-received-test");
    assertThat(handler.stringPayloads.poll(5, TimeUnit.SECONDS)).isNotNull();

    Counter counter =
        meterRegistry
            .find("nats.listener.messages.received")
            .tag("subject", "combo.string")
            .tag("queue", "")
            .counter();
    assertThat(counter).isNotNull();
    assertThat(Objects.requireNonNull(counter).count()).isEqualTo(countBefore + 1.0);
  }

  @Test
  void givenNatsListener_whenMessageProcessedSuccessfully_thenSuccessCounterIncrementedByOne()
      throws Exception {
    Counter before =
        meterRegistry
            .find("nats.listener.messages.success")
            .tag("subject", "combo.string")
            .tag("queue", "")
            .counter();
    double countBefore = before != null ? before.count() : 0.0;

    natsOperations.publish("combo.string", "metrics-success-test");
    assertThat(handler.stringPayloads.poll(5, TimeUnit.SECONDS)).isNotNull();

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Counter counter =
                  meterRegistry
                      .find("nats.listener.messages.success")
                      .tag("subject", "combo.string")
                      .tag("queue", "")
                      .counter();
              assertThat(counter).isNotNull();
              assertThat(Objects.requireNonNull(counter).count()).isEqualTo(countBefore + 1.0);
            });
  }

  @Test
  void givenNatsListener_whenInvalidMessageReceived_thenErrorCounterIncrementedByOne() {
    Counter before =
        meterRegistry
            .find("nats.listener.messages.error")
            .tag("subject", "combo.object")
            .tag("queue", "")
            .counter();
    double countBefore = before != null ? before.count() : 0.0;

    natsOperations.publish("combo.object", "not-valid-json");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              Counter counter =
                  meterRegistry
                      .find("nats.listener.messages.error")
                      .tag("subject", "combo.object")
                      .tag("queue", "")
                      .counter();
              assertThat(counter).isNotNull();
              assertThat(Objects.requireNonNull(counter).count()).isEqualTo(countBefore + 1.0);
            });
  }

  @Test
  void givenNatsListener_whenMessageProcessed_thenDurationTimerIncrementedByOne() throws Exception {
    Timer before =
        meterRegistry
            .find("nats.listener.messages.duration")
            .tag("subject", "combo.string")
            .tag("queue", "")
            .timer();
    long countBefore = before != null ? before.count() : 0L;

    natsOperations.publish("combo.string", "metrics-duration-test");
    assertThat(handler.stringPayloads.poll(5, TimeUnit.SECONDS)).isNotNull();

    Timer timer =
        meterRegistry
            .find("nats.listener.messages.duration")
            .tag("subject", "combo.string")
            .tag("queue", "")
            .timer();
    assertThat(timer).isNotNull();
    assertThat(Objects.requireNonNull(timer).count()).isEqualTo(countBefore + 1L);
  }
}
