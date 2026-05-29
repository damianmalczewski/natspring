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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.malczuuu.natspring.connection.JetStreamConfigurer;
import io.github.malczuuu.natspring.core.StreamConfigureException;
import io.nats.client.Options;
import io.nats.client.api.StreamConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class JetStreamConfigurerTests extends AbstractIntegrationTests {

  @Test
  void givenNoStreams_whenStart_thenShouldNotFail() {
    Options options =
        new Options.Builder()
            .server(nats.getConnectionUrl())
            .userInfo(nats.getUsername(), nats.getPassword())
            .build();

    JetStreamConfigurer configurer = new JetStreamConfigurer(options, true, List.of());
    configurer.start();

    assertThat(configurer.isRunning()).isTrue();
  }

  @Test
  void givenConflictingSubjects_whenStart_thenThrowsStreamConfigureException() {
    Options options =
        new Options.Builder()
            .server(nats.getConnectionUrl())
            .userInfo(nats.getUsername(), nats.getPassword())
            .build();

    JetStreamConfigurer configurer =
        new JetStreamConfigurer(
            options,
            true,
            List.of(
                StreamConfiguration.builder()
                    .name("CONFLICT_DIRECT_ALPHA")
                    .subjects("conflict.direct.subject")
                    .build(),
                StreamConfiguration.builder()
                    .name("CONFLICT_DIRECT_BETA")
                    .subjects("conflict.direct.subject")
                    .build()));

    assertThatThrownBy(configurer::start).isInstanceOf(StreamConfigureException.class);
  }

  @Test
  void givenDuplicateStream_whenStartCalledTwice_thenNoExceptionThrown() {
    Options options =
        new Options.Builder()
            .server(nats.getConnectionUrl())
            .userInfo(nats.getUsername(), nats.getPassword())
            .build();

    StreamConfiguration stream =
        StreamConfiguration.builder()
            .name("DUPLICATE_STREAM")
            .subjects("duplicate.stream.subject")
            .build();
    JetStreamConfigurer configurer = new JetStreamConfigurer(options, true, List.of(stream));

    configurer.start();

    assertThatCode(configurer::start).doesNotThrowAnyException();
  }
}
