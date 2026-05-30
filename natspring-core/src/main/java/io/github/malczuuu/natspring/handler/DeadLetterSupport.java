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

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

final class DeadLetterSupport {

  private static final int DETAIL_MAX_LENGTH = 200;

  /**
   * Builds a base set of dead-letter headers from the original message. Copies all original headers
   * and adds {@code X-Dead-Letter-Subject}, {@code X-Dead-Letter-Reason}, and {@code
   * X-Dead-Letter-Timestamp}. Callers may add further headers before publishing.
   */
  static Headers buildDeadLetterHeaders(
      Message message, String sourceSubject, @Nullable Throwable cause) {
    Headers headers = new Headers();
    Headers origHeaders = message.getHeaders();
    if (origHeaders != null) {
      for (String key : origHeaders.keySet()) {
        List<String> values = origHeaders.get(key);
        if (values != null && !values.isEmpty()) {
          headers.add(key, values);
        }
      }
    }
    headers.add("X-Dead-Letter-Subject", sourceSubject);
    if (cause != null) {
      Throwable root =
          cause instanceof InvocationTargetException ite && ite.getCause() != null
              ? ite.getCause()
              : cause;
      String exceptionMessage = root.getMessage();
      String reason =
          root.getClass().getSimpleName()
              + (exceptionMessage != null ? ": " + truncate(exceptionMessage) : "");
      headers.add("X-Dead-Letter-Reason", reason);
      headers.add("X-Dead-Letter-Exception", root.getClass().getName());
    }
    headers.add("X-Dead-Letter-Timestamp", Instant.now().toString());
    return headers;
  }

  /**
   * Publishes a dead-letter message. Throws if the publish fails - callers decide whether to
   * propagate or swallow.
   */
  static void buildAndPublishDeadLetter(
      Connection connection, String deadLetterSubject, Message message, Headers headers) {
    byte[] body = message.getData() != null ? message.getData() : new byte[0];
    Message deadLetterMessage =
        NatsMessage.builder().subject(deadLetterSubject).headers(headers).data(body).build();
    connection.publish(deadLetterMessage);
  }

  private static String truncate(String value) {
    String sanitized = value.replace('\n', ' ').replace('\r', ' ');
    return sanitized.length() <= DETAIL_MAX_LENGTH
        ? sanitized
        : sanitized.substring(0, DETAIL_MAX_LENGTH) + "...";
  }

  private DeadLetterSupport() {}
}
