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

package io.github.malczuuu.natspring.itest;

import io.github.malczuuu.natspring.annotation.NatsHeader;
import io.github.malczuuu.natspring.annotation.NatsHeaders;
import io.github.malczuuu.natspring.annotation.NatsListener;
import io.github.malczuuu.natspring.annotation.NatsPayload;
import io.github.malczuuu.natspring.annotation.NatsReplyTo;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.jspecify.annotations.Nullable;
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
  public final BlockingQueue<Message> deadLetterMessages = new LinkedBlockingQueue<>();
  public final BlockingQueue<Message> rpcMessages = new LinkedBlockingQueue<>();

  @NatsListener(subject = "combo.no-args")
  public void handleNoArgs() {
    noArgsLatch.countDown();
  }

  @NatsListener(subject = "combo.message")
  public void handleMessage(Message message) {
    messages.add(message);
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

  @NatsListener(subject = "combo.dlq-source", deadLetterSubject = "combo.dead-letter")
  public void handleDlqSource(String data) {
    throw new RuntimeException("simulated failure for dlq test");
  }

  @NatsListener(subject = "combo.dead-letter")
  public void handleDeadLetter(Message message) {
    deadLetterMessages.add(message);
  }

  @NatsListener(subject = "rpc.bytes")
  public byte[] handleRpcBytes(byte[] payload) {
    return payload;
  }

  @NatsListener(subject = "rpc.string")
  public String handleRpcString(String payload) {
    return payload.toUpperCase(Locale.ROOT);
  }

  @NatsListener(subject = "rpc.object")
  public SampleMessage handleRpcObject(SampleMessage payload) {
    return new SampleMessage(payload.name().toUpperCase(Locale.ROOT), payload.value() * 2);
  }

  @NatsListener(subject = "rpc.message")
  public Message handleRpcMessage(Message message) {
    rpcMessages.add(message);
    return NatsMessage.builder()
        .subject(message.getReplyTo() != null ? message.getReplyTo() : "")
        .data(
            ("echo:" + new String(message.getData(), StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8))
        .build();
  }

  @NatsListener(subject = "rpc.reply-to-param")
  public void handleRpcWithReplyToParam(String payload, @NatsReplyTo @Nullable String replyTo) {
    replyToValues.add(replyTo != null ? replyTo : "");
  }

  @NatsListener(subject = "rpc.no-reply-to")
  public String handleRpcNoReplyTo(String payload) {
    return payload + "-reply";
  }

  public final BlockingQueue<String> replyToValues = new LinkedBlockingQueue<>();

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
    deadLetterMessages.clear();
    replyToValues.clear();
    rpcMessages.clear();
  }
}
