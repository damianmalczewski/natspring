# Natspring Project

[![Build Status](https://github.com/damianmalczewski/natspring/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/damianmalczewski/natspring/actions/workflows/gradle-build.yml)
[![Xodecov](https://codecov.io/gh/damianmalczewski/natspring/graph/badge.svg?token=6NQ031426J)](https://codecov.io/gh/damianmalczewski/natspring)
[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-starter)][maven-central]
[![License](https://img.shields.io/github/license/damianmalczewski/natspring)](https://github.com/damianmalczewski/natspring/blob/main/LICENSE)

Spring Boot auto-configuration for [NATS](https://nats.io), providing annotation-driven message listeners for both core
NATS and JetStream.

Requires Spring Boot 4.x.

> [!IMPORTANT]
> This project began as an educational exercise for learning how to create `@KafkaListener`-alike, annotation-based
> message listeners. It has no roadmap and its evolution will most likely be driven by personal interest and/or
> contributions.

## Table of Contents

- [Installation](#installation)
- [Configuration](#configuration)
    - [Options customizer](#options-customizer)
    - [Connection hooks](#connection-hooks)
- [Listener annotations](#listener-annotations)
    - [`@NatsListener`](#natslistener)
    - [`@JetStreamListener`](#jetstreamlistener)
- [Dead-lettering](#dead-lettering)
    - [Core NATS](#core-nats)
    - [JetStream](#jetstream)
    - [Dead-letter headers](#dead-letter-headers)
- [Method parameter types](#method-parameter-types)
    - [Parameter annotations](#parameter-annotations)
        - [`@NatsPayload`](#natspayload)
        - [`@NatsHeader`](#natsheader)
        - [`@NatsHeaders`](#natsheaders)
        - [`@NatsSubject`](#natssubject)
        - [`@NatsReplyTo`](#natsreplyto)
    - [JSON deserialization](#json-deserialization)
    - [No-arg methods](#no-arg-methods)
    - [Mixed parameters](#mixed-parameters)
    - [Message interceptors](#message-interceptors)
- [Request-reply (RPC)](#request-reply-rpc)
    - [Listener return values](#listener-return-values)
    - [Sending requests](#sending-requests)
- [JetStream stream auto-creation](#jetstream-stream-auto-creation)
- [NatsOperations](#natsoperations)
    - [Publishing](#publishing)
    - [Headers](#headers)
    - [Publish interceptors](#publish-interceptors)
- [Observability](#observability)
    - [Health](#health)
    - [Metrics](#metrics)
- [Testing](#testing)
    - [Testcontainers](#testcontainers)
- [Building from source](#building-from-source)
- [License](#license)

## Installation

[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-starter)][maven-central]

Add the starter module to your dependencies.

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-starter</artifactId>
    <version>{version}</version>
</dependency>
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-starter-test</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

```kotlin
dependencies {
    implementation("io.github.malczuuu.natspring:natspring-starter:{version}")
    testImplementation("io.github.malczuuu.natspring:natspring-starter-test:{version}")
}
```

## Configuration

- `nats.enabled` - Whether NATS auto-configuration is enabled. Default: `true`.
- `nats.server` - NATS server URL. Maps to `server(String)`. Default: `nats://localhost:4222`.
- `nats.username` - Username for NATS authentication. Combined with `password` into `userInfo(String, char[])`. Default: _(none)_.
- `nats.password` - Password for NATS authentication. Combined with `username` into `userInfo(String, char[])`. Default: _(none)_.
- `nats.auto-stream-creation` - Whether declared `StreamConfiguration` beans are used to create streams on startup. Default: `false`.
- `nats.pull-fetch-batch-size` - Number of messages fetched per poll cycle for JetStream pull consumers. Default: `200`.
- `nats.pull-fetch-timeout` - Maximum time to wait for messages in each fetch call for JetStream pull consumers. Default: `200ms`.

<details>
<summary><b>Additional properties (see more...)</b></summary>

All properties in this section are nullable. Setting a property to `null` skips the related `io.nats.client.Options`
call and the NATS Java client's built-in default applies.

- `nats.connection-name` - Name for the NATS connection; falls back to `spring.application.name`. Maps to `connectionName(String)`. Default: `null`.
- `nats.connection-timeout` - Maximum time to wait when establishing a connection. Maps to `connectionTimeout(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.socket-write-timeout` - Maximum time to wait for a socket write to complete. Maps to `socketWriteTimeout(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.max-reconnects` - Maximum reconnect attempts before giving up; `-1` means unlimited. Maps to `maxReconnects(int)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.reconnect-wait` - Time to wait between reconnect attempts. Maps to `reconnectWait(Duration)`. Default: `null` (`2s` per NATS Java client).
- `nats.reconnect-jitter` - Random jitter added to `reconnect-wait` for non-TLS connections. Maps to `reconnectJitter(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.reconnect-jitter-tls` - Random jitter added to `reconnect-wait` for TLS connections. Maps to `reconnectJitterTls(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.reconnect-buffer-size` - Size in bytes of the buffer used to hold messages while reconnecting. Maps to `reconnectBufferSize(long)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.ping-interval` - Interval between client-side pings to the server. Maps to `pingInterval(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.max-pings-out` - Maximum outstanding pings without a response before the connection is considered stale. Maps to `maxPingsOut(int)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.request-cleanup-interval` - Interval at which the client scans for timed-out pending requests. Maps to `requestCleanupInterval(Duration)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.inbox-prefix` - Prefix for auto-generated inbox subjects (must end with `.`). Maps to `inboxPrefix(String)`. Default: `null` (so the value of native NATS client is not overwritten).
- `nats.no-echo` - Suppress echoing published messages back to the sending connection. Maps to `noEcho()`. Default: `false`.
- `nats.no-randomize` - Disable randomization of the server list on connect and reconnect. Maps to `noRandomize()`. Default: `false`.

</details>

### Options customizer

For connection options not covered by `nats.*` properties, declare a `ConnectionOptionsBuilderCustomizer`bean. It
receives an `io.nats.client.Options.Builder` before the connection is established and can apply any configuration the
native NATS Java client supports.

```java
@Configuration
public class NatsCustomizerConfiguration {

  @Bean
  public ConnectionOptionsBuilderCustomizer tlsCustomizer() {
    return builder -> builder.sslContext(buildSslContext());
  }
}
```

Multiple customizer beans are applied in the order determined by `org.springframework.core.Ordered`. Customizers run
after the built-in property-based configuration, so they take precedence over `nats.*` properties for the same option.

### Connection hooks

> [!IMPORTANT]
> This feature is experimental and most likely will change after more use cases are discovered.

The native `io.nats.client.Connection` cannot be injected as a Spring bean. Current library manages it internally via
Spring's `SmartLifecycle` and controls when it is opened and closed. For example, when `@SpringBootTest`-annotated tests
switch context due to different Spring configurations, the subscriptions are stopped, the connection is closed, and the
fresh one for new context is created and started. When we go back to the previous context, the connection is recreated
again and so on. One connection never interferes with another.

Any logic that depends directly on the native connection - such as creating subscriptions without annotations provided
by current library, dispatchers, or key-value store handles - must be performed through a `ConnectionHook` bean. This
ensures that such resources are properly set up after each connect and torn down before each close.

```java
@Component
public class CustomSubscriptionHook implements ConnectionHook {

  @Override
  public void postConnect(Connection connection) {
    connection.createDispatcher().subscribe("custom.subject", msg -> {
      // handle message
    });
  }
  
  // you can also override preClose(Connection connection)
}
```

Multiple `ConnectionHook` beans are invoked in the order determined by `org.springframework.core.Ordered`.

## Listener annotations

### `@NatsListener`

Subscribes to a core NATS subject. Supports queue groups for load balancing.

```java
@NatsListener(subject = "orders.placed")
public void onOrder(Order order) {}

@NatsListener(subject = "orders.placed", queue = "order-processors")
public void onOrderQueued(Order order) {}
```

| Attribute           | Description                                                                                  |
|---------------------|----------------------------------------------------------------------------------------------|
| `subject`           | NATS subject pattern (wildcards `*` and `>` supported). Supports `${property}` placeholders. |
| `queue`             | Optional queue group name for competing-consumer load balancing.                             |
| `deadLetterSubject` | Optional subject to publish failed messages to. Empty string (default) disables DLQ.         |

### `@JetStreamListener`

Subscribes to a JetStream subject using a durable push consumer with explicit ack. On successful return the message is
acked; on handler exception it is nacked; on deserialization failure it is terminated.

```java
@JetStreamListener(subject = "orders.>", stream = "ORDERS", durable = "order-processor")
public void onOrder(Order order) {}

@JetStreamListener(
    subject = "orders.>",
    stream = "ORDERS",
    durable = "order-proc-grp",
    queue = "processors")
public void onOrderQueued(Order order) {}
```

| Attribute              | Description                                                                                |
|------------------------|--------------------------------------------------------------------------------------------|
| `subject`              | Subject pattern to filter within the stream. Supports `${property}` placeholders.          |
| `stream`               | JetStream stream name. Optional; NATS will infer from the subject if omitted.              |
| `durable`              | Durable consumer name. Omit for an ephemeral consumer.                                     |
| `queue`                | Optional queue group name for competing-consumer load balancing.                           |
| `deadLetterSubject`    | Optional subject to publish failed messages to. Empty string (default) disables DLQ.       |
| `deadLetterDeliveries` | Maximum delivery attempts before dead-lettering. Required when `deadLetterSubject` is set. |
| `ackMode`              | `AUTO` (default) acks on success and nacks on failure; `MANUAL` leaves ack to the handler. |
| `deliverPolicy`        | Which messages to receive on first connect: `NEW` (default), `ALL`, or `LAST`.             |
| `consumerType`         | `PULL` (default) or `PUSH`.                                                                |

## Dead-lettering

Both listener types support a `deadLetterSubject` attribute. When set, failed messages are published to that subject
instead of being silently dropped. All original message headers are forwarded, and additional `X-Dead-Letter-*` headers
are added (see [Dead-letter headers](#dead-letter-headers) below).

### Core NATS

Core NATS has no persistence or redelivery. Dead-lettering is **at-most-once**: a failure publishes to the DLQ
immediately and the original message is gone regardless.

```java
@NatsListener(subject = "orders.placed", deadLetterSubject = "orders.placed.dlq")
public void onOrder(Order order) { /* ... */ }
```

Both argument resolution failures (malformed payload) and handler invocation failures dead-letter on the first attempt.
If the DLQ publish itself fails, the error is logged and the message is dropped.

### JetStream

JetStream has persistence and delivery tracking, so dead-lettering integrates with the retry lifecycle:

```java
@JetStreamListener(
    subject = "orders.>",
    stream = "ORDERS",
    durable = "order-processor",
    deadLetterSubject = "orders.dlq",
    maxDeliveries = 3)
public void onOrder(Order order) { /* ... */ }
```

| Failure type                | Behaviour                                                                                                 |
|-----------------------------|-----------------------------------------------------------------------------------------------------------|
| Argument resolution failure | Message published to DLQ immediately, then `term()`-ed. Retrying a malformed payload would never succeed. |
| Handler invocation failure  | Message is `nak()`-ed and redelivered up to `maxDeliveries` times, then published to DLQ and `term()`-ed. |

If the DLQ publish itself fails, the exception propagates: the message is **not** terminated and will be redelivered.
This may push the delivery count above `maxDeliveries`, which is intentional - the message is retried until the DLQ
becomes reachable rather than being lost.

### Dead-letter headers

Every dead-letter message carries the following headers in addition to all headers from the original message.

- `X-Dead-Letter-Subject` - original subject the message was received on                                    
- `X-Dead-Letter-Reason` - exception simple name and message, truncated to 200 characters                  
- `X-Dead-Letter-Exception` - fully-qualified exception class name                                            
- `X-Dead-Letter-Timestamp` - ISO-8601 UTC timestamp of the dead-letter publish                               
- `X-Dead-Letter-Stream` - JetStream stream name, not available from `@NatsListener`                       
- `X-Dead-Letter-Durable` - durable consumer name, not available from `@NatsListener`                       
- `X-Dead-Letter-Delivery` - delivery count at the time of dead-lettering, not available from `@NatsListener`

## Method parameter types

Parameters are resolved in the order listed below. The first match wins.

| Priority | Condition                                                                | Resolved value                                             |
|----------|--------------------------------------------------------------------------|------------------------------------------------------------|
| 1        | Parameter type is `io.nats.client.Message` (or subtype)                  | Raw NATS message                                           |
| 2        | Parameter annotated with `@NatsHeader`                                   | Header value(s) as `String`, `List<String>`, or `String[]` |
| 3        | Parameter annotated with `@NatsSubject`                                  | Message subject as `String`                                |
| 4        | Parameter annotated with `@NatsHeaders`                                  | All headers as `io.nats.client.impl.Headers`               |
| 5        | Parameter type is `io.nats.client.impl.Headers` (without `@NatsPayload`) | All headers as `io.nats.client.impl.Headers`               |
| 6        | Parameter type is `NatsJetStreamMetaData` (without `@NatsPayload`)       | JetStream message metadata                                 |
| 7        | Parameter type is `byte[]`                                               | Raw message body bytes                                     |
| 8        | Parameter type is `String`                                               | Message body decoded as UTF-8                              |
| 9        | Any other type, or `@NatsPayload`-annotated parameter                    | Message body deserialized from JSON                        |

### Parameter annotations

### `@NatsPayload`

Marks a parameter explicitly as the message payload. If the type is easily distinguishable (`byte[]`, `String` or a POJO
class/record), the annotation can be omitted and the parameter will be resolved as payload by default. Recommended
keeping for clarity and/or documentation purposes.

```java
@NatsListener(subject = "raw.events")
public void handle(@NatsPayload byte[] body) { /* ... */ }

@NatsListener(subject = "text.events")
public void handle(@NatsPayload String text) { /* ... */ }

@NatsListener(subject = "json.events")
public void handle(@NatsPayload List<Event> events) { /* ... */ }
```

### `@NatsHeader`

Injects a header value by name. Resolved as `String` (first value), `List<String>`, or `String[]` (all values) depending
on the parameter type.

```java
@NatsListener(subject = "events")
public void handle(Event event, @NatsHeader("X-Correlation-Id") String correlationId) { /* ... */ }

@NatsListener(subject = "events")
public void handle(@NatsHeader("X-Tags") List<String> tags) { /* ... */ }

@NatsListener(subject = "events")
public void handle(@NatsHeader("X-Tags") String[] tags) { /* ... */ }
```

### `@NatsHeaders`

Injects all message headers. Equivalent to declaring `io.nats.client.impl.Headers` as the parameter type, but explicit.

```java
@NatsListener(subject = "events")
public void handle(Event event, @NatsHeaders Headers headers) { /* ... */ }
```

### `@NatsSubject`

Injects the subject the message was published to. Useful when a listener matches a wildcard subject
and needs to inspect the concrete subject at runtime.

```java
@NatsListener(subject = "events.>")
public void handle(Event event, @NatsSubject String subject) {}

@JetStreamListener(subject = "orders.>", stream = "ORDERS", durable = "router")
public void handle(Order order, @NatsSubject String subject) { /* ... */ }
```

### `@NatsReplyTo`

Injects the reply-to inbox from the incoming message as a `String`. The value is `null` when the message has no reply-to
address (i.e., was published without a reply inbox). Only supported on `@NatsListener` methods.

```java
@NatsListener(subject = "events")
public void handle(Event event, @NatsReplyTo String replyTo) {
    if (replyTo != null) {
        natsOperations.publish(replyTo, new EventAck(event.id()));
    }
}
```

### JSON deserialization

Any parameter not matched by the rules above is deserialized from the message body using Jackson. Full generic type
information is preserved, so `List<Order>`, `Order[]`, and other parameterized types work correctly.

```java
@NatsListener(subject = "batch.orders")
public void onBatch(List<Order> orders) { /* ... */ }

@NatsListener(subject = "batch.orders")
public void onBatch(Order[] orders) { /* ... */ }
```

### No-arg methods

Methods with no parameters are supported. The message is received and discarded.

```java
@NatsListener(subject = "ping")
public void onPing() { /* ... */ }
```

### Mixed parameters

A method may declare any combination of the above in any order.

```java
@JetStreamListener(subject = "orders.>", stream = "ORDERS", durable = "auditor")
public void onOrder(
    Order order, @NatsHeader("X-Source") String source, Headers allHeaders, Message rawMessage) {
  // ...
}
```

### Message interceptors

`NatsMessageInterceptor` beans are discovered automatically and applied to every inbound message in order, for both
`@NatsListener` and `@JetStreamListener` methods. The interceptor receives the raw `io.nats.client.Message` before any
argument resolution or deserialization.

```java
@Component
public class TracingMessageInterceptor implements NatsMessageInterceptor {

  private final Tracer tracer;

  public TracingMessageInterceptor(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public void intercept(Message message, NatsMessageInterceptorChain chain) {
    String traceId =
        message.getHeaders() != null ? message.getHeaders().getFirst("X-Trace-Id") : null;
    tracer.setCurrentTraceId(traceId != null ? traceId : tracer.newTraceId());
    try {
      chain.proceed(message);
    } finally {
      tracer.clearCurrentTraceId();
    }
  }
}
```

Not calling `chain.proceed()` drops the message silently. For JetStream listeners, a dropped message will be
redelivered by the broker unless the interceptor explicitly acknowledges or terminates it beforehand.

Multiple interceptors are sorted by their `getOrder()` value (`Ordered.LOWEST_PRECEDENCE` by default).

## Request-reply (RPC)

NATS supports a built-in request-reply (RPC) pattern. The requester sends a message with a reply-to inbox address; the
responder publishes the reply to that address.

### Listener return values

A `@NatsListener` method can participate as a responder by returning a non-void value. When the incoming message
carries a reply-to address, the return value is automatically serialized and published to that address.

| Return type              | Reply body                                 |
|--------------------------|--------------------------------------------|
| `byte[]`                 | Sent as-is                                 |
| `String`                 | UTF-8 encoded bytes                        |
| `io.nats.client.Message` | Body and headers from the returned message |
| Any other type           | JSON-serialized via Jackson                |

```java
@NatsListener(subject = "calc.add")
public MathResult handleAdd(MathRequest request) {
    return new MathResult(request.a() + request.b());
}

@NatsListener(subject = "ping")
public String handlePing() {
    return "pong";
}
```

If the incoming message has no reply-to address and the method returns a non-null value, the reply is discarded and a
`WARN` is logged. This allows a subject to be used for both fire-and-forget and RPC without reconfiguring the listener.

Return values are not supported on `@JetStreamListener` methods. JetStream uses the reply-to field internally for
acknowledgment.

### Sending requests

`NatsOperations` provides `request()` methods that send a message and return a `CompletableFuture<NatsReply>` that
completes when a reply arrives. The future completes exceptionally with `TimeoutException` if no reply arrives within
the given timeout.

```java
class RequestExample {
    
  private final NatsOperations natsOperations;

  public void requestExamples() {
    // bytes
    NatsReply reply = natsOperations.request("ping", new byte[0], Duration.ofSeconds(5)).get();

    // string
    NatsReply reply = natsOperations.request("ping", "data", Duration.ofSeconds(5)).get();

    // JSON-serialized object - reply deserialized with bodyAs()
    NatsReply reply =
        natsOperations.request("calc.add", new MathRequest(3, 4), Duration.ofSeconds(5)).get();
    MathResult result = reply.bodyAs(MathResult.class);
  }
}
```

`NatsReply` wraps the raw `io.nats.client.Message` and adds `bodyAs(Class<T>)` / `bodyAs(TypeReference<T>)` for
JSON deserialization without a separate `ObjectMapper`.

## JetStream stream auto-creation

Declare `io.nats.client.api.StreamConfiguration` beans and the auto-configuration will create the
corresponding streams on startup (if they do not already exist), before any listeners are registered.

> [!IMPORTANT]
> Works only if `nats.auto-stream-creation` is set to `true` (disabled by default).

```java
@Configuration
public class ExampleConfiguration {

  @Bean
  public StreamConfiguration ordersStream() {
    return StreamConfiguration.builder().name("ORDERS").subjects("orders.>").build();
  }
}
```

## NatsOperations

`NatsOperations` is the primary API for publishing messages and sending requests. It is auto-configured as a Spring bean
and available for injection.

```java
@Service
public class OrderService {

  private final NatsOperations natsOperations;

  public OrderService(NatsOperations natsOperations) {
    this.natsOperations = natsOperations;
  }
}
```

### Publishing

Three payload types are supported out of the box:

```java
class OrderService {

  private final NatsOperations natsOperations;

  void publishExamples() {
    natsOperations.publish("orders.placed", rawBytes); // byte[]
    natsOperations.publish("orders.placed", "plain text"); // String - UTF-8 encoded
    natsOperations.publish("orders.placed", new Order("id", "...")); // JSON via Jackson
  }
}
```

A pre-built `io.nats.client.Message` can be published as-is for full control:

```java
class OrderService {

  private final NatsOperations natsOperations;

  void publishMessage() {
    Message message = NatsMessage.builder().subject("orders.placed").data(rawBytes).build();
    natsOperations.publish(message);
  }
}
```

### Headers

Every publish variant has an overload that accepts `io.nats.client.impl.Headers`:

```java
class OrderService {

  private final NatsOperations natsOperations;
  private final AppInfo appInfo;

  void publishWithHeaders() {
    Headers headers = new Headers();
    headers.add("X-Publisher-App", appInfo.getAppName());

    natsOperations.publish("orders.placed", headers, rawBytes); // byte[]
    natsOperations.publish("orders.placed", headers, "plain text"); // String - UTF-8 encoded
    natsOperations.publish("orders.placed", headers, new Order("id", "...")); // JSON via Jackson
  }
}
```

`request()` does not have a headers overload - attach headers via a [publish interceptor](#publish-interceptors) or
build a `Message` and use `publish()` manually.

### Publish interceptors

`NatsPublishInterceptor` beans are discovered automatically and applied to every outbound message in order, including
requests. Interceptors receive a fully-built `Message` (subject, headers, serialized body) regardless of which
`publish()` or `request()` overload was called.

```java
@Component
public class TracingPublishInterceptor implements NatsPublishInterceptor {

  private final Tracer tracer;

  public TracingPublishInterceptor(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public void intercept(Message message, NatsPublishInterceptorChain chain) {
    Headers enriched = message.getHeaders() != null ? message.getHeaders() : new Headers();
    enriched.add("X-Trace-Id", tracer.currentTraceId());
    chain.proceed(
        NatsMessage.builder()
            .subject(message.getSubject())
            .headers(enriched)
            .data(message.getData())
            .build());
  }
}
```

Not calling `chain.proceed()` suppresses the message entirely. For requests, a suppressed message causes the returned
`CompletableFuture` to complete exceptionally with `IllegalStateException`.

Multiple interceptors are sorted by their `getOrder()` value (`Ordered.LOWEST_PRECEDENCE` by default).

## Observability

### Health

When `spring-boot-actuator` is on the classpath, a `NatsHealthIndicator` is auto-configured under the `nats` component
name. It reports `UP` when the connection status is `CONNECTED` and `DOWN` for any other status, including connection
loss or failure to obtain the connection.

```json
{
  "components": {
    "nats": {
      "status": "UP",
      "details": { "connectionStatus": "CONNECTED" }
    }
  }
}
```

### Metrics

When Micrometer is on the classpath, the following meters are auto-configured. All observer beans can be replaced by
declaring a custom implementation in the application context.

<details>
<summary><b>Core NATS listener metrics (see more...)</b></summary>

Tagged with `subject` and `queue`.

| Meter                                 | Type    | Description                                 |
|---------------------------------------|---------|---------------------------------------------|
| `nats.listener.messages.received`     | Counter | Messages received before handler invocation |
| `nats.listener.messages.success`      | Counter | Messages handled without exception          |
| `nats.listener.messages.error`        | Counter | Messages that caused a handler exception    |
| `nats.listener.messages.deadlettered` | Counter | Messages published to a dead-letter subject |
| `nats.listener.messages.duration`     | Timer   | Handler processing time                     |

</details>

<details>
<summary><b>JetStream listener metrics (see more...)</b></summary>

Tagged with `subject` and `stream`.

| Meter                                  | Type    | Description                                                                       |
|----------------------------------------|---------|-----------------------------------------------------------------------------------|
| `nats.jetstream.messages.received`     | Counter | Messages received before handler invocation                                       |
| `nats.jetstream.messages.acked`        | Counter | Messages acked after successful handling                                          |
| `nats.jetstream.messages.nacked`       | Counter | Messages nacked after a handler exception                                         |
| `nats.jetstream.messages.terminated`   | Counter | Messages terminated (e.g. deserialization failure). Also tagged with `exception`. |
| `nats.jetstream.messages.deadlettered` | Counter | Messages dead-lettered after exhausting delivery attempts                         |
| `nats.jetstream.messages.duration`     | Timer   | Handler processing time                                                           |

</details>

<details>
<summary><b>Connection metrics (see more...)</b></summary>

| Meter                                        | Type    | Tags        | Description                                      |
|----------------------------------------------|---------|-------------|--------------------------------------------------|
| `nats.connection.events`                     | Counter | `nats`      | Connection state-change events                   |
| `nats.connection.errors`                     | Counter | `error`     | Server error strings received                    |
| `nats.connection.exceptions`                 | Counter | `exception` | Client-side exceptions during processing         |
| `nats.connection.slow.consumer.detected`     | Counter | -           | Slow consumer detections                         |
| `nats.connection.message.discarded`          | Counter | -           | Messages discarded due to a full consumer queue  |
| `nats.statistics.pings`                      | Gauge   | -           | Total pings sent                                 |
| `nats.statistics.reconnects`                 | Gauge   | -           | Total reconnect attempts                         |
| `nats.statistics.in.msgs`                    | Gauge   | -           | Total inbound messages                           |
| `nats.statistics.out.msgs`                   | Gauge   | -           | Total outbound messages                          |
| `nats.statistics.in.bytes`                   | Gauge   | -           | Total inbound bytes                              |
| `nats.statistics.out.bytes`                  | Gauge   | -           | Total outbound bytes                             |
| `nats.statistics.dropped.count`              | Gauge   | -           | Messages dropped across all slow consumers       |
| `nats.statistics.flush.counter`              | Gauge   | -           | Outgoing message flushes                         |
| `nats.statistics.outstanding.requests`       | Gauge   | -           | Outstanding request count                        |
| `nats.statistics.oks`                        | Gauge   | -           | Op `+OK` messages received                       |
| `nats.statistics.errs`                       | Gauge   | -           | Op `-ERR` messages received                      |
| `nats.statistics.exceptions`                 | Gauge   | -           |                                                  |
| `nats.statistics.requests.sent`              | Gauge   | -           | Requests sent                                    |
| `nats.statistics.replies.received`           | Gauge   | -           | Replies received                                 |
| `nats.statistics.duplicate.replies.received` | Gauge   | -           | Duplicate replies received (advanced stats only) |
| `nats.statistics.orphan.replies.received`    | Gauge   | -           | Orphan replies received (advanced stats only)    |

</details>

## Testing

Add `natspring-starter-test` to your test dependencies:

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-starter-test</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

### Testcontainers

There is no official Testcontainers module for NATS. The community-maintained
[`io.github.amadeusitgroup.testcontainers:nats`][nats-testcontainers-java] library provides a `NatsContainer`. Current
library integrates it with Spring Boot's `@ServiceConnection` for zero-config wiring.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.github.amadeusitgroup.testcontainers</groupId>
    <artifactId>nats</artifactId>
    <version>1.1.4</version>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
class MyIntegrationTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14");
}
```

`@ServiceConnection` auto-configures `nats.server` from the running container - no manual property overrides needed.

[nats-testcontainers-java]: https://github.com/AmadeusITGroup/nats-testcontainers-java

## Building from source

<details>
<summary><b>Expand...</b></summary>

Gradle **9.x+** requires **Java 17** or higher to run. For building the project, Gradle automatically picks up **Java
25** via **toolchains** and the `foojay-resolver-convention` plugin. This Java version is needed because the project
uses **ErrorProne** and **NullAway** for static nullness analysis.

The produced artifacts are compatible with **Java 17** thanks to `options.release = 17` in Gradle `JavaCompile` tasks.
This means that regardless of the Java version used to run Gradle, the resulting bytecode remains compatible.

The **default Gradle tasks** include `spotlessApply` (for code formatting) and `build` (for compilation and tests). The
simplest way to build the project is to run:

```bash
./gradlew
```

**Note** that the `natspring-integration-tests` module uses Testcontainers to spin up a real NATS server. **Docker must
be running** for these tests to pass.

---

To **execute tests** use `test` task. Tests do not change `options.release` so newer Java API can be used.

```bash
./gradlew test
```

---

To **format the code** according to the style defined in [`build.gradle.kts`](./build.gradle.kts) rules use `spotlessApply`
task. **Note** that **building will fail** if code is not properly formatted.

```bash
./gradlew spotlessApply
```

**Note** that if the year has changed, add `-Pspotless.license-year-enabled` flag to update the year in license headers.

```bash
./gradlew spotlessApply -Pspotless.license-year-enabled
```

---

To **publish** the built artifacts to **local Maven repository**, use `publishToMavenLocal` task.

```bash
./gradlew publishToMavenLocal
```

Note that for using Maven Local artifacts in target projects, you need to add `mavenLocal()` repository.

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}
```

</details>

## License

This project is licensed under the Apache License, Version 2.0.

This project is not affiliated with, sponsored by, or endorsed by Spring Boot or NATS. All product names, logos, and
brands are property of their respective owners.

[maven-central]: https://central.sonatype.com/artifact/io.github.malczuuu.natspring/natspring-starter
