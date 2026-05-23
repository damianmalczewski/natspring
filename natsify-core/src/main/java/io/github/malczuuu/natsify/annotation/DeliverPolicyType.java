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

package io.github.malczuuu.natsify.annotation;

/** Starting point for message delivery when a consumer is created. */
public enum DeliverPolicyType {

  /** All messages retained in the stream are delivered. */
  ALL,

  /** Only messages published after consumer creation are delivered. */
  NEW,

  /** Only the most recent message at consumer creation time is delivered. */
  LAST
}
