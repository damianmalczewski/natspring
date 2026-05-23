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

import io.github.malczuuu.natsify.annotation.NatsHeader;
import io.github.malczuuu.natsify.annotation.NatsHeaders;
import io.github.malczuuu.natsify.annotation.NatsListener;
import io.github.malczuuu.natsify.annotation.NatsPayload;
import io.github.malczuuu.natsify.itest.model.SampleMessage;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class NatsListenerComponent {

  public final CountDownLatch noArgsLatch = new CountDownLatch(1);
  public final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
  public final BlockingQueue<byte[]> bytesPayloads = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> stringPayloads = new LinkedBlockingQueue<>();
  public final BlockingQueue<byte[]> bytesWithPayloadAnnotation = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> stringsWithPayloadAnnotation = new LinkedBlockingQueue<>();
  public final BlockingQueue<SampleMessage> objects = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> headerValues = new LinkedBlockingQueue<>();
  public final BlockingQueue<Headers> headersValues = new LinkedBlockingQueue<>();
  public final BlockingQueue<Headers> headersValuesByType = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> queueGroupMessages = new LinkedBlockingQueue<>();
  public final BlockingQueue<String> propertySubjectMessages = new LinkedBlockingQueue<>();
  public final BlockingQueue<List<SampleMessage>> genericLists = new LinkedBlockingQueue<>();
  public final BlockingQueue<SampleMessage[]> arrays = new LinkedBlockingQueue<>();

  @NatsListener(subject = "combo.no-args")
  public void handleNoArgs() {
    noArgsLatch.countDown();
  }

  @NatsListener(subject = "combo.message")
  public void handleMessage(Message msg) {
    messages.add(msg);
  }

  @NatsListener(subject = "combo.bytes")
  public void handleBytes(byte[] data) {
    bytesPayloads.add(data);
  }

  @NatsListener(subject = "combo.string")
  public void handleString(String data) {
    stringPayloads.add(data);
  }

  @NatsListener(subject = "combo.bytes-payload")
  public void handleBytesWithPayload(@NatsPayload byte[] data) {
    bytesWithPayloadAnnotation.add(data);
  }

  @NatsListener(subject = "combo.string-payload")
  public void handleStringWithPayload(@NatsPayload String data) {
    stringsWithPayloadAnnotation.add(data);
  }

  @NatsListener(subject = "combo.object")
  public void handleObject(SampleMessage payload) {
    objects.add(payload);
  }

  @NatsListener(subject = "combo.header")
  public void handleHeader(@NatsHeader("X-Key") String value) {
    headerValues.add(value);
  }

  @NatsListener(subject = "combo.headers")
  public void handleHeaders(@NatsHeaders Headers allHeaders) {
    headersValues.add(allHeaders);
  }

  @NatsListener(subject = "combo.queue-queue", queue = "test-queue")
  public void handleQueueGroup(String data) {
    queueGroupMessages.add(data);
  }

  @NatsListener(subject = "${nats.combo.subject}", queue = "${nats.combo.queue}")
  public void handlePropertyResolved(String data) {
    propertySubjectMessages.add(data);
  }

  @NatsListener(subject = "combo.generic-list")
  public void handleGenericList(List<SampleMessage> payloads) {
    genericLists.add(payloads);
  }

  @NatsListener(subject = "combo.array")
  public void handleArray(SampleMessage[] payloads) {
    arrays.add(payloads);
  }

  @NatsListener(subject = "combo.headers-by-type")
  public void handleHeadersByType(Headers allHeaders) {
    headersValuesByType.add(allHeaders);
  }

  public void clearAll() {
    messages.clear();
    bytesPayloads.clear();
    stringPayloads.clear();
    bytesWithPayloadAnnotation.clear();
    stringsWithPayloadAnnotation.clear();
    objects.clear();
    headerValues.clear();
    headersValues.clear();
    headersValuesByType.clear();
    queueGroupMessages.clear();
    propertySubjectMessages.clear();
    genericLists.clear();
    arrays.clear();
  }
}
