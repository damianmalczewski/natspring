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

package io.github.malczuuu.natspring.testcontainers;

import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.github.malczuuu.natspring.autoconfigure.NatsConnectionDetails;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class NatsContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<NatsContainer, NatsConnectionDetails> {

  @Override
  protected NatsConnectionDetails getContainerConnectionDetails(
      ContainerConnectionSource<NatsContainer> source) {
    return new NatsContainerConnectionDetails(source);
  }

  private static final class NatsContainerConnectionDetails
      extends ContainerConnectionDetails<NatsContainer> implements NatsConnectionDetails {

    private NatsContainerConnectionDetails(ContainerConnectionSource<NatsContainer> source) {
      super(source);
    }

    @Override
    public String getServer() {
      return "nats://" + getContainer().getHost() + ":" + getContainer().getClientPort();
    }

    @Override
    public @Nullable String getUsername() {
      return getContainer().getUsername();
    }

    @Override
    public @Nullable String getPassword() {
      return getContainer().getPassword();
    }
  }
}
