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

package io.github.malczuuu.natspring.core;

/**
 * Callback for customizing a {@link NatsOperations.Builder} before the instance is built. Register
 * implementations as Spring beans to apply cross-cutting configuration (e.g. interceptors, JSON
 * mapper overrides) without replacing the entire auto-configured instance.
 *
 * @since 0.4.0
 */
@FunctionalInterface
public interface NatsOperationsBuilderCustomizer {

  /**
   * Applies customizations to the given builder.
   *
   * @param builder the builder to customize
   * @return the customized builder
   */
  NatsOperations.Builder customize(NatsOperations.Builder builder);
}
