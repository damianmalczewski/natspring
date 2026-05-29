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

package org.example.natspring.request;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class RequestReplyExampleTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14.0");

  @Autowired RestTestClient restClient;

  @Test
  void addEndpointSendsRequestAndReturnsSum() {
    restClient
        .get()
        .uri("/add?a=3&b=4")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(MathResult.class)
        .value(result -> assertThat(result.sum()).isEqualTo(7));
  }

  @Test
  void echoEndpointSendsRequestAndReturnsText() {
    restClient
        .get()
        .uri("/echo?text=hello")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(result -> assertThat(result).isEqualTo("hello"));
  }
}
