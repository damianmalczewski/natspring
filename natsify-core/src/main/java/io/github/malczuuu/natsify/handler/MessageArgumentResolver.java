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

package io.github.malczuuu.natsify.handler;

import io.nats.client.Message;
import java.lang.reflect.Parameter;
import org.jspecify.annotations.Nullable;

/** Resolves method arguments from a NATS {@link Message}. */
public interface MessageArgumentResolver {

  /**
   * Resolves all parameters for a listener method from the given message.
   *
   * @param parameters the method parameters to resolve
   * @param message the received message
   * @return array of resolved arguments, or {@code null}
   */
  Object @Nullable [] resolveArguments(Parameter[] parameters, Message message);

  /**
   * Resolves a single method parameter from the given message.
   *
   * @param parameter the method parameter to resolve
   * @param message the received message
   * @return the resolved argument, or {@code null}
   */
  @Nullable Object resolveArgument(Parameter parameter, Message message);
}
