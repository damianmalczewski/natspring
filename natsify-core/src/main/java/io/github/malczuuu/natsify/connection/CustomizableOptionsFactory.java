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

package io.github.malczuuu.natsify.connection;

import io.nats.client.Options;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConnectionOptionsFactory} that builds {@link Options} by applying a list of {@link
 * ConnectionOptionsBuilderCustomizer} instances in registration order.
 */
public final class CustomizableOptionsFactory implements ConnectionOptionsFactory {

  private final List<ConnectionOptionsBuilderCustomizer> customizers = new ArrayList<>();

  /** Creates a new {@code CustomizableOptionsFactory} with no registered customizers. */
  public CustomizableOptionsFactory() {}

  /**
   * Registers a customizer to be applied when building connection options.
   *
   * @param customizer the customizer to register
   */
  public void registerBuilderCustomizer(ConnectionOptionsBuilderCustomizer customizer) {
    customizers.add(customizer);
  }

  /**
   * Returns NATS connection options built by applying all registered customizers in registration
   * order.
   *
   * @return the built {@link Options}
   */
  @Override
  public Options getOptions() {
    Options.Builder builder = new Options.Builder();
    for (ConnectionOptionsBuilderCustomizer customizer : customizers) {
      builder = customizer.customize(builder);
    }
    return builder.build();
  }
}
