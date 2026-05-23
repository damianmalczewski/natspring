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

package io.github.malczuuu.natsify.itest.infra;

import io.github.malczuuu.natsify.annotation.ConsumerType;
import io.github.malczuuu.natsify.annotation.JetStreamListener;
import io.github.malczuuu.natsify.itest.model.SampleMessage;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class JetStreamListenerComponent {

  public final BlockingQueue<String> strings = new LinkedBlockingQueue<>();
  public final BlockingQueue<byte[]> bytes = new LinkedBlockingQueue<>();
  public final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
  public final BlockingQueue<SampleMessage> objects = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> queueGroupMessages = new LinkedBlockingQueue<>();
  public final BlockingQueue<List<SampleMessage>> genericLists = new LinkedBlockingQueue<>();
  public final BlockingQueue<SampleMessage[]> arrays = new LinkedBlockingQueue<>();
  public final BlockingQueue<Headers> headersValuesByType = new LinkedBlockingQueue<>();

  @JetStreamListener(
      subject = "js.string",
      stream = "TEST",
      durable = "string-consumer",
      consumerType = ConsumerType.PUSH)
  public void handleString(String data) {
    strings.add(data);
  }

  @JetStreamListener(subject = "js.bytes", stream = "TEST", durable = "bytes-consumer")
  public void handleBytes(byte[] data) {
    bytes.add(data);
  }

  @JetStreamListener(subject = "js.message", stream = "TEST", durable = "message-consumer")
  public void handleMessage(Message msg) {
    messages.add(msg);
  }

  @JetStreamListener(
      subject = "js.object",
      stream = "TEST",
      durable = "object-consumer",
      consumerType = ConsumerType.PUSH)
  public void handleObject(SampleMessage payload) {
    objects.add(payload);
  }

  @JetStreamListener(
      subject = "js.queue",
      stream = "TEST",
      durable = "queue-consumer",
      queue = "test-queue-queue")
  public void handleQueueGroup(String data) {
    queueGroupMessages.add(data);
  }

  @JetStreamListener(
      subject = "js.generic-list",
      stream = "TEST",
      durable = "generic-list-consumer")
  public void handleGenericList(List<SampleMessage> payloads) {
    genericLists.add(payloads);
  }

  @JetStreamListener(subject = "js.array", stream = "TEST", durable = "array-consumer")
  public void handleArray(SampleMessage[] payloads) {
    arrays.add(payloads);
  }

  @JetStreamListener(
      subject = "js.headers-by-type",
      stream = "TEST",
      durable = "headers-by-type-consumer",
      consumerType = ConsumerType.PUSH)
  public void handleHeadersByType(Headers allHeaders) {
    headersValuesByType.add(allHeaders);
  }

  public void clearAll() {
    strings.clear();
    bytes.clear();
    messages.clear();
    objects.clear();
    queueGroupMessages.clear();
    genericLists.clear();
    arrays.clear();
    headersValuesByType.clear();
  }
}
