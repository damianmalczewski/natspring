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

import io.github.malczuuu.natspring.annotation.NatsHeader;
import io.github.malczuuu.natspring.annotation.NatsHeaders;
import io.github.malczuuu.natspring.annotation.NatsReplyTo;
import io.github.malczuuu.natspring.annotation.NatsSubject;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

final class ListenerMethodValidation {

  static void validateNatsListenerMethod(Method method) {
    validate(method, false);
  }

  static void validateJetStreamListenerMethod(Method method) {
    validate(method, true);
  }

  private static void validate(Method method, boolean jetStream) {
    Parameter[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      validateParameter(method, params[i], i, jetStream);
    }
  }

  private static void validateParameter(
      Method method, Parameter param, int index, boolean jetStream) {
    if (!jetStream && NatsJetStreamMetaData.class.isAssignableFrom(param.getType())) {
      throw new IllegalArgumentException(jetStreamMetaDataNotAllowed(method, index));
    }
    if (param.isAnnotationPresent(NatsReplyTo.class)) {
      if (jetStream) {
        throw new IllegalArgumentException(natsReplyToNotAllowedOnJetStream(method, index));
      }
      if (param.getType() != String.class) {
        throw new IllegalArgumentException(natsReplyToMustBeString(method, index));
      }
    }
    NatsHeader natsHeader = param.getAnnotation(NatsHeader.class);
    if (natsHeader != null) {
      if (natsHeader.value().isEmpty() && !param.isNamePresent()) {
        throw new IllegalArgumentException(natsHeaderRequiresName(method, index));
      }
      Class<?> type = param.getType();
      if (type == String.class || type == String[].class) {
        // valid
      } else if (List.class.isAssignableFrom(type)) {
        Type genericType = param.getParameterizedType();
        if (!(genericType instanceof ParameterizedType paramType)
            || paramType.getActualTypeArguments()[0] != String.class) {
          throw new IllegalArgumentException(natsHeaderListMustBeStringList(method, index));
        }
      } else {
        throw new IllegalArgumentException(natsHeaderUnsupportedType(method, index));
      }
    }
    if (param.isAnnotationPresent(NatsSubject.class)) {
      if (param.getType() != String.class) {
        throw new IllegalArgumentException(natsSubjectMustBeString(method, index));
      }
    }
    if (param.isAnnotationPresent(NatsHeaders.class)) {
      if (!Headers.class.isAssignableFrom(param.getType())) {
        throw new IllegalArgumentException(natsHeadersMustBeAssignableFromHeaders(method, index));
      }
    }
  }

  private static String jetStreamMetaDataNotAllowed(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": NatsJetStreamMetaData is only allowed in @JetStreamListener methods";
  }

  private static String natsHeaderRequiresName(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsHeader requires a non-empty value or compilation with -parameters";
  }

  private static String natsHeaderListMustBeStringList(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsHeader List parameter must be List<String>";
  }

  private static String natsHeaderUnsupportedType(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsHeader parameter must be String, String[], or List<String>";
  }

  private static String natsSubjectMustBeString(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsSubject parameter must be String";
  }

  private static String natsHeadersMustBeAssignableFromHeaders(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsHeaders parameter must be assignable from "
        + Headers.class.getName();
  }

  private static String natsReplyToNotAllowedOnJetStream(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsReplyTo is not supported on @JetStreamListener methods";
  }

  private static String natsReplyToMustBeString(Method method, int index) {
    return "Parameter "
        + index
        + " of "
        + method.toGenericString()
        + ": @NatsReplyTo parameter must be String";
  }

  private ListenerMethodValidation() {}
}
