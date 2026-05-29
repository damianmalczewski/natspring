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

package org.example.natspring.request;

import io.github.malczuuu.natspring.annotation.NatsListener;
import io.github.malczuuu.natspring.core.NatsOperations;
import io.github.malczuuu.natspring.core.NatsReply;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class RequestReplyExample {

  private static final Logger log = LoggerFactory.getLogger(RequestReplyExample.class);

  private final NatsOperations natsOperations;

  public RequestReplyExample(NatsOperations natsOperations) {
    this.natsOperations = natsOperations;
  }

  @GetMapping("/add")
  public MathResult add(@RequestParam int a, @RequestParam int b) throws Exception {
    NatsReply reply =
        natsOperations.request("calc.add", new MathRequest(a, b), Duration.ofSeconds(5)).get();
    return reply.bodyAs(MathResult.class);
  }

  @GetMapping("/echo")
  public String echo(@RequestParam String text) throws Exception {
    NatsReply reply = natsOperations.request("calc.echo", text, Duration.ofSeconds(5)).get();
    return new String(reply.getMessage().getData(), StandardCharsets.UTF_8);
  }

  @NatsListener(subject = "calc.add")
  public MathResult handleAdd(MathRequest request) {
    log.info("Handling calc.add: {}+{}", request.a(), request.b());
    return new MathResult(request.a() + request.b());
  }

  @NatsListener(subject = "calc.echo")
  public String handleEcho(String text) {
    log.info("Handling calc.echo: {}", text);
    return text;
  }

  public static void main(String[] args) {
    SpringApplication.run(RequestReplyExample.class, args);
  }
}
