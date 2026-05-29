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

import io.github.malczuuu.natspring.connection.ConnectionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = Entrypoint.class)
@DirtiesContext
class ContextLifecycleTests extends AbstractIntegrationTests {

  @Autowired private ConnectionManager connectionManager;

  @Test
  void givenStartedContext_whenContextCloses_thenConnectionManagerStopsCleanly() {
    assertThat(connectionManager.isRunning()).isTrue();
  }
}
