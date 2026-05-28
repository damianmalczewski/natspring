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

package io.github.malczuuu.natsify.core;

import io.nats.client.Message;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

final class NatsPublishInterceptorChainExecution {

  private final List<NatsPublishInterceptor> interceptors;

  NatsPublishInterceptorChainExecution(List<NatsPublishInterceptor> interceptors) {
    this.interceptors =
        interceptors.stream()
            .sorted(Comparator.comparingInt(NatsPublishInterceptor::getOrder))
            .toList();
  }

  void execute(Message message, Consumer<Message> target) {
    new Chain(0, target).proceed(message);
  }

  private final class Chain implements NatsPublishInterceptorChain {

    private final int index;
    private final Consumer<Message> target;

    private Chain(int index, Consumer<Message> target) {
      this.index = index;
      this.target = target;
    }

    @Override
    public void proceed(Message message) {
      if (index < interceptors.size()) {
        interceptors.get(index).intercept(message, new Chain(index + 1, target));
      } else {
        target.accept(message);
      }
    }
  }
}
