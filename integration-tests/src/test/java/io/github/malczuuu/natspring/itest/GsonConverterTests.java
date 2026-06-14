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

import io.github.malczuuu.natspring.converter.gson.GsonNatsMessageConverter;
import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.itest.entrypoint.Entrypoint;
import io.github.malczuuu.natspring.itest.entrypoint.NatsListenerComponent;
import io.github.malczuuu.natspring.itest.entrypoint.SampleMessage;
import io.github.malczuuu.natspring.itest.fixture.AbstractSpringBootTests;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(classes = {Entrypoint.class, GsonConverterTests.GsonConfig.class})
class GsonConverterTests extends AbstractSpringBootTests {

  @TestConfiguration
  static class GsonConfig {

    @Bean
    GsonNatsMessageConverter gsonNatsMessageConverter() {
      return new GsonNatsMessageConverter();
    }
  }

  @Autowired private NatsListenerComponent handler;
  @Autowired private NatsOperations natsClient;

  @AfterEach
  void afterEach() {
    handler.clearAll();
  }

  @Test
  void givenObjectSubject_whenJsonMessagePublished_thenHandlerDeserializesObject()
      throws Exception {
    natsClient.publish("combo.object", new SampleMessage("gson-test", 42));

    SampleMessage received = handler.objects.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received.name()).isEqualTo("gson-test");
    assertThat(received.value()).isEqualTo(42);
  }

  @Test
  void givenGenericListSubject_whenJsonMessagePublished_thenHandlerDeserializesList()
      throws Exception {
    natsClient.publish(
        "combo.generic-list", List.of(new SampleMessage("a", 1), new SampleMessage("b", 2)));

    List<SampleMessage> received = handler.genericLists.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received.get(0).name()).isEqualTo("a");
    assertThat(received.get(1).name()).isEqualTo("b");
  }

  @Test
  void givenArraySubject_whenJsonMessagePublished_thenHandlerDeserializesArray() throws Exception {
    natsClient.publish(
        "combo.array", List.of(new SampleMessage("c", 3), new SampleMessage("d", 4)));

    SampleMessage[] received = handler.arrays.poll(10, TimeUnit.SECONDS);
    assertThat(received).isNotNull();
    assertThat(received).hasSize(2);
    assertThat(received[0].name()).isEqualTo("c");
    assertThat(received[1].name()).isEqualTo("d");
  }
}
