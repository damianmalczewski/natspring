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

package io.github.malczuuu.natspring.namastack.outbox;

import io.namastack.outbox.handler.OutboxRecordMetadata;
import io.namastack.outbox.routing.OutboxRoute;
import io.namastack.outbox.routing.OutboxRoutingConfigurer;
import io.namastack.outbox.routing.selector.OutboxPayloadSelector;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

final class DefaultNatsOutboxRouting extends NatsOutboxRouting {

  public DefaultNatsOutboxRouting(List<OutboxRoute> rules, @Nullable OutboxRoute defaultRule) {
    super(rules, defaultRule);
  }

  @Override
  public String resolveSubject(Object payload, OutboxRecordMetadata metadata) {
    return resolveTarget(payload, metadata);
  }

  static final class Builder implements NatsOutboxRouting.Builder {

    private final OutboxRoutingConfigurer configurer = new OutboxRoutingConfigurer();

    @Override
    public Builder route(
        OutboxPayloadSelector selector, Consumer<OutboxRoute.Builder> routeConfigurer) {
      configurer.route(selector, routeConfigurer);
      return this;
    }

    @Override
    public Builder defaults(Consumer<OutboxRoute.Builder> routeConfigurer) {
      configurer.defaults(routeConfigurer);
      return this;
    }

    @Override
    public DefaultNatsOutboxRouting build() {
      return new DefaultNatsOutboxRouting(configurer.rules(), configurer.defaultRule());
    }
  }
}
