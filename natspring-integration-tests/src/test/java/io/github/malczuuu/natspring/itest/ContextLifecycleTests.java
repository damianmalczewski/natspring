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

package io.github.malczuuu.natspring.itest;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.malczuuu.natspring.health.NatsHealthIndicator;
import io.github.malczuuu.natspring.itest.generic.AbstractSpringBootTests;
import io.nats.client.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Status;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
class ContextLifecycleTests extends AbstractSpringBootTests {

  @Autowired private NatsHealthIndicator healthIndicator;

  @Test
  void givenStartedContext_whenContextCloses_thenConnectionManagerStopsCleanly() {
    assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
    assertThat(healthIndicator.health().getDetails().get("connectionStatus"))
        .isEqualTo(Connection.Status.CONNECTED);
  }
}
