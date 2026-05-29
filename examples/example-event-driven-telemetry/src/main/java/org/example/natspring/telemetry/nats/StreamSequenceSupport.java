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

package org.example.natspring.telemetry.nats;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import org.springframework.stereotype.Component;

@Component
public class StreamSequenceSupport {

  public String build(String streamName, long sequence) {
    return hash(streamName) + "-" + String.format("%012d", sequence);
  }

  private String hash(String streamName) {
    CRC32 crc = new CRC32();
    crc.update(streamName.getBytes(StandardCharsets.UTF_8));
    return String.format("%08x", crc.getValue());
  }
}
