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

package io.github.malczuuu.natspring.handler;

import io.nats.client.Message;
import java.lang.reflect.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Resolves method arguments from a NATS {@link Message} and converts listener return values into
 * reply messages.
 *
 * @since 0.1.0
 */
public interface MessageArgumentResolver {

  /**
   * Resolves all parameters for a listener method from the given message.
   *
   * @param parameters the method parameters to resolve
   * @param message the received message
   * @return array of resolved arguments, or {@code null}
   * @since 0.1.0
   */
  Object @Nullable [] resolveArguments(Parameter[] parameters, Message message);

  /**
   * Resolves a single method parameter from the given message.
   *
   * @param parameter the method parameter to resolve
   * @param message the received message
   * @return the resolved argument, or {@code null}
   * @since 0.1.0
   */
  @Nullable Object resolveArgument(Parameter parameter, Message message);

  /**
   * Converts a listener method return value into a reply {@link Message} addressed to the given
   * subject. Supports {@link Message}, {@code byte[]}, {@link String}, and arbitrary objects
   * (serialized to JSON).
   *
   * @param result the non-null return value from the listener method
   * @param replyTo the NATS subject to address the reply to
   * @return the reply message ready to publish
   * @since 0.1.0
   */
  Message buildReplyMessage(Object result, String replyTo);
}
