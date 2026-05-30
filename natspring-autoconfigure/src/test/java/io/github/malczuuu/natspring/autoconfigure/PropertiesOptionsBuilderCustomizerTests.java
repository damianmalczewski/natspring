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

package io.github.malczuuu.natspring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.malczuuu.natspring.connection.DefaultConnectionOptionsFactory;
import io.nats.client.Options;
import java.net.URI;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class PropertiesOptionsBuilderCustomizerTests {

  private static final String SERVER = "nats://localhost:4222";

  private MockEnvironment environment;
  private NatsProperties properties;

  @BeforeEach
  void beforeEach() {
    environment = new MockEnvironment();
    properties = new NatsProperties();
  }

  private Options customize() {
    return customize(null, null);
  }

  private Options customize(@Nullable String username, @Nullable String password) {
    NatsConnectionDetails details = new StubConnectionDetails(SERVER, username, password);
    DefaultConnectionOptionsFactory factory = new DefaultConnectionOptionsFactory();
    factory.registerCustomizer(
        new PropertiesOptionsBuilderCustomizer(environment, properties, details));
    return factory.getOptions();
  }

  @Test
  void givenServer_whenCustomize_thenServerApplied() {
    Options options = customize();

    assertThat(options.getServers()).extracting(URI::toString).contains(SERVER);
  }

  @Test
  void givenConnectionName_whenCustomize_thenConnectionNameApplied() {
    properties.setConnectionName("my-connection");

    Options options = customize();

    assertThat(options.getConnectionName()).isEqualTo("my-connection");
  }

  @Test
  void givenNoConnectionName_whenCustomize_thenFallsBackToAppName() {
    environment.setProperty("spring.application.name", "my-service");

    Options options = customize();

    assertThat(options.getConnectionName()).isEqualTo("my-service");
  }

  @Test
  void givenNoConnectionNameAndNoAppName_whenCustomize_thenConnectionNameIsNull() {
    Options options = customize();

    assertThat(options.getConnectionName()).isNull();
  }

  @Test
  void givenCredentials_whenCustomize_thenNoException() {
    Options options = customize("user", "secret");

    assertThat(options).isNotNull();
  }

  @Test
  void givenNoEchoTrue_whenCustomize_thenNoEchoEnabled() {
    properties.setNoEcho(true);

    Options options = customize();

    assertThat(options.isNoEcho()).isTrue();
  }

  @Test
  void givenNoEchoFalse_whenCustomize_thenNoEchoDisabled() {
    Options options = customize();

    assertThat(options.isNoEcho()).isFalse();
  }

  @Test
  void givenNoRandomizeTrue_whenCustomize_thenNoRandomizeEnabled() {
    properties.setNoRandomize(true);

    Options options = customize();

    assertThat(options.isNoRandomize()).isTrue();
  }

  @Test
  void givenNoRandomizeFalse_whenCustomize_thenNoRandomizeDisabled() {
    Options options = customize();

    assertThat(options.isNoRandomize()).isFalse();
  }

  @Test
  void givenInboxPrefix_whenCustomize_thenInboxPrefixApplied() {
    properties.setInboxPrefix("CUSTOM.");

    Options options = customize();

    assertThat(options.getInboxPrefix()).isEqualTo("CUSTOM.");
  }

  @Test
  void givenNoInboxPrefix_whenCustomize_thenDefaultInboxPrefixPreserved() {
    Options options = customize();

    assertThat(options.getInboxPrefix()).isEqualTo(Options.DEFAULT_INBOX_PREFIX);
  }

  @Test
  void givenConnectionTimeout_whenCustomize_thenConnectionTimeoutApplied() {
    properties.setConnectionTimeout(Duration.ofSeconds(10));

    Options options = customize();

    assertThat(options.getConnectionTimeout()).isEqualTo(Duration.ofSeconds(10));
  }

  @Test
  void givenNoConnectionTimeout_whenCustomize_thenDefaultConnectionTimeoutPreserved() {
    Options options = customize();

    assertThat(options.getConnectionTimeout()).isEqualTo(Options.DEFAULT_CONNECTION_TIMEOUT);
  }

  @Test
  void givenSocketWriteTimeout_whenCustomize_thenSocketWriteTimeoutApplied() {
    properties.setSocketWriteTimeout(Duration.ofSeconds(30));

    Options options = customize();

    assertThat(options.getSocketWriteTimeout()).isEqualTo(Duration.ofSeconds(30));
  }

  @Test
  void givenNoSocketWriteTimeout_whenCustomize_thenDefaultSocketWriteTimeoutPreserved() {
    Options options = customize();

    assertThat(options.getSocketWriteTimeout()).isEqualTo(Options.DEFAULT_SOCKET_WRITE_TIMEOUT);
  }

  @Test
  void givenMaxReconnects_whenCustomize_thenMaxReconnectsApplied() {
    properties.setMaxReconnects(5);

    Options options = customize();

    assertThat(options.getMaxReconnect()).isEqualTo(5);
  }

  @Test
  void givenNoMaxReconnects_whenCustomize_thenDefaultMaxReconnectsPreserved() {
    Options options = customize();

    assertThat(options.getMaxReconnect()).isEqualTo(Options.DEFAULT_MAX_RECONNECT);
  }

  @Test
  void givenReconnectWait_whenCustomize_thenReconnectWaitApplied() {
    properties.setReconnectWait(Duration.ofSeconds(5));

    Options options = customize();

    assertThat(options.getReconnectWait()).isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  void givenNoReconnectWait_whenCustomize_thenDefaultReconnectWaitPreserved() {
    Options options = customize();

    assertThat(options.getReconnectWait()).isEqualTo(Options.DEFAULT_RECONNECT_WAIT);
  }

  @Test
  void givenReconnectJitter_whenCustomize_thenReconnectJitterApplied() {
    properties.setReconnectJitter(Duration.ofMillis(500));

    Options options = customize();

    assertThat(options.getReconnectJitter()).isEqualTo(Duration.ofMillis(500));
  }

  @Test
  void givenNoReconnectJitter_whenCustomize_thenDefaultReconnectJitterPreserved() {
    Options options = customize();

    assertThat(options.getReconnectJitter()).isEqualTo(Options.DEFAULT_RECONNECT_JITTER);
  }

  @Test
  void givenReconnectJitterTls_whenCustomize_thenReconnectJitterTlsApplied() {
    properties.setReconnectJitterTls(Duration.ofSeconds(2));

    Options options = customize();

    assertThat(options.getReconnectJitterTls()).isEqualTo(Duration.ofSeconds(2));
  }

  @Test
  void givenNoReconnectJitterTls_whenCustomize_thenDefaultReconnectJitterTlsPreserved() {
    Options options = customize();

    assertThat(options.getReconnectJitterTls()).isEqualTo(Options.DEFAULT_RECONNECT_JITTER_TLS);
  }

  @Test
  void givenReconnectBufferSize_whenCustomize_thenReconnectBufferSizeApplied() {
    properties.setReconnectBufferSize(1024L);

    Options options = customize();

    assertThat(options.getReconnectBufferSize()).isEqualTo(1024L);
  }

  @Test
  void givenNoReconnectBufferSize_whenCustomize_thenDefaultReconnectBufferSizePreserved() {
    Options options = customize();

    assertThat(options.getReconnectBufferSize()).isEqualTo(Options.DEFAULT_RECONNECT_BUF_SIZE);
  }

  @Test
  void givenPingInterval_whenCustomize_thenPingIntervalApplied() {
    properties.setPingInterval(Duration.ofMinutes(5));

    Options options = customize();

    assertThat(options.getPingInterval()).isEqualTo(Duration.ofMinutes(5));
  }

  @Test
  void givenNoPingInterval_whenCustomize_thenDefaultPingIntervalPreserved() {
    Options options = customize();

    assertThat(options.getPingInterval()).isEqualTo(Options.DEFAULT_PING_INTERVAL);
  }

  @Test
  void givenMaxPingsOut_whenCustomize_thenMaxPingsOutApplied() {
    properties.setMaxPingsOut(5);

    Options options = customize();

    assertThat(options.getMaxPingsOut()).isEqualTo(5);
  }

  @Test
  void givenNoMaxPingsOut_whenCustomize_thenDefaultMaxPingsOutPreserved() {
    Options options = customize();

    assertThat(options.getMaxPingsOut()).isEqualTo(Options.DEFAULT_MAX_PINGS_OUT);
  }

  @Test
  void givenRequestCleanupInterval_whenCustomize_thenRequestCleanupIntervalApplied() {
    properties.setRequestCleanupInterval(Duration.ofSeconds(10));

    Options options = customize();

    assertThat(options.getRequestCleanupInterval()).isEqualTo(Duration.ofSeconds(10));
  }

  @Test
  void givenNoRequestCleanupInterval_whenCustomize_thenDefaultRequestCleanupIntervalPreserved() {
    Options options = customize();

    assertThat(options.getRequestCleanupInterval())
        .isEqualTo(Options.DEFAULT_REQUEST_CLEANUP_INTERVAL);
  }

  private record StubConnectionDetails(
      String server, @Nullable String username, @Nullable String password)
      implements NatsConnectionDetails {

    @Override
    public String getServer() {
      return server;
    }

    @Override
    public @Nullable String getUsername() {
      return username;
    }

    @Override
    public @Nullable String getPassword() {
      return password;
    }
  }
}
