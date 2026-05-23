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

package io.github.malczuuu.natsify.autoconfigure;

import org.jspecify.annotations.Nullable;

final class PropertiesNatsConnectionDetails implements NatsConnectionDetails {

  private final NatsProperties properties;

  PropertiesNatsConnectionDetails(NatsProperties properties) {
    this.properties = properties;
  }

  @Override
  public String getServer() {
    return properties.getServer();
  }

  @Override
  public @Nullable String getUsername() {
    return properties.getUsername();
  }

  @Override
  public @Nullable String getPassword() {
    return properties.getPassword();
  }
}
