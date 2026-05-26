# Natsify Project

Spring Boot auto-configuration for [NATS](https://nats.io), providing annotation-driven message listeners for both core
NATS and JetStream.

Requires Spring Boot 4.x.

> [!IMPORTANT]
> This project is currently an educational exercise for learning how to create `@KafkaListener`-alike annotation-based
> message listeners for NATS. Currently, it has lots of limitations or issues that would need to be addressed before it
> can be used anywhere outside a sandbox.

## Table of Contents

- [Configuration](#configuration)
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
        - [`@NatsSubject`](#natssubject)
        - [`@NatsHeaders`](#natsheaders)
    - [JSON deserialization](#json-deserialization)
    - [No-arg methods](#no-arg-methods)
    - [Mixed parameters](#mixed-parameters)
- [JetStream stream auto-creation](#jetstream-stream-auto-creation)
- [Publishing messages](#publishing-messages)
- [Observability](#observability)
    - [Health](#health)
    - [Metrics](#metrics)
- [Testing](#testing)
    - [Testcontainers](#testcontainers)

## Configuration

> [!IMPORTANT]
> Default values of properties that directly configure `io.nats.client.Options` are taken from static defaults in the
> NATS Java client.

| Property                           | Default                 | Description                                                                                                         |
|------------------------------------|-------------------------|---------------------------------------------------------------------------------------------------------------------|
| `natsify.enabled`                  | `true`                  | Whether NATS auto-configuration is enabled.                                                                         |
| `natsify.server`                   | `nats://localhost:4222` | NATS server URL. Maps to `server(String)`.                                                                          |
| `natsify.username`                 | _(none)_                | Username for NATS authentication. Combined with `password` into `userInfo(String, char[])`.                         |
| `natsify.password`                 | _(none)_                | Password for NATS authentication. Combined with `username` into `userInfo(String, char[])`.                         |
| `natsify.connection-name`          | _(none)_                | Name for the NATS connection; falls back to `spring.application.name`. Maps to `connectionName(String)`.            |
| `natsify.connection-timeout`       | `2s`                    | Maximum time to wait when establishing a connection. Maps to `connectionTimeout(Duration)`.                         |
| `natsify.socket-write-timeout`     | `1m`                    | Maximum time to wait for a socket write to complete. Maps to `socketWriteTimeout(Duration)`.                        |
| `natsify.max-reconnects`           | `60`                    | Maximum reconnect attempts before giving up; `-1` means unlimited. Maps to `maxReconnects(int)`.                    |
| `natsify.reconnect-wait`           | `2s`                    | Time to wait between reconnect attempts. Maps to `reconnectWait(Duration)`.                                         |
| `natsify.reconnect-jitter`         | `100ms`                 | Random jitter added to `reconnect-wait` for non-TLS connections. Maps to `reconnectJitter(Duration)`.               |
| `natsify.reconnect-jitter-tls`     | `1s`                    | Random jitter added to `reconnect-wait` for TLS connections. Maps to `reconnectJitterTls(Duration)`.                |
| `natsify.reconnect-buffer-size`    | `8388608`               | Size in bytes of the buffer used to hold messages while reconnecting (8 MB). Maps to `reconnectBufferSize(long)`.   |
| `natsify.ping-interval`            | `2m`                    | Interval between client-side pings to the server. Maps to `pingInterval(Duration)`.                                 |
| `natsify.max-pings-out`            | `2`                     | Maximum outstanding pings without a response before the connection is considered stale. Maps to `maxPingsOut(int)`. |
| `natsify.request-cleanup-interval` | `5s`                    | Interval at which the client scans for timed-out pending requests. Maps to `requestCleanupInterval(Duration)`.      |
| `natsify.inbox-prefix`             | _(none)_                | Prefix for auto-generated inbox subjects (must end with `.`); defaults to `_INBOX.`. Maps to `inboxPrefix(String)`. |
| `natsify.no-echo`                  | `false`                 | Suppress echoing published messages back to the sending connection. Maps to `noEcho()`.                             |
| `natsify.no-randomize`             | `false`                 | Disable randomization of the server list on connect and reconnect. Maps to `noRandomize()`.                         |
| `natsify.auto-stream-creation`     | `false`                 | Whether declared `StreamConfiguration` beans are used to create or update streams on startup.                       |
| `natsify.pull-fetch-batch-size`    | `200`                   | Number of messages fetched per poll cycle for JetStream pull consumers.                                             |
| `natsify.pull-fetch-timeout`       | `200ms`                 | Maximum time to wait for messages in each fetch call for JetStream pull consumers.                                  |

## Listener annotations

### `@NatsListener`

Subscribes to a core NATS subject. Supports queue groups for load balancing.

```java
@NatsListener(subject = "orders.placed")
public void onOrder(Order order) {}

@NatsListener(subject = "orders.placed", group = "order-processors")
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

| Attribute           | Description                                                                                       |
|---------------------|---------------------------------------------------------------------------------------------------|
| `subject`           | Subject pattern to filter within the stream. Supports `${property}` placeholders.                 |
| `stream`            | JetStream stream name. Optional; NATS will infer from the subject if omitted.                     |
| `durable`           | Durable consumer name. Omit for an ephemeral consumer.                                            |
| `queue`             | Optional queue group name for competing-consumer load balancing.                                  |
| `deadLetterSubject` | Optional subject to publish failed messages to. Empty string (default) disables DLQ.              |
| `maxDeliveries`     | Maximum delivery attempts before dead-lettering. Required when `deadLetterSubject` is set.        |
| `ackMode`           | `AUTO` (default) acks on success and nacks on failure; `MANUAL` leaves ack to the handler.        |
| `deliverPolicy`     | Which messages to receive on first connect: `NEW` (default), `ALL`, or `LAST`.                    |
| `consumerType`      | `PULL` (default) or `PUSH`.                                                                       |

## Dead-lettering

Both listener types support a `deadLetterSubject` attribute. When set, failed messages are published to that subject
instead of being silently dropped. All original message headers are forwarded, and additional `X-Dead-Letter-*` headers
are added (see [Dead-letter headers](#dead-letter-headers) below).

### Core NATS

Core NATS has no persistence or redelivery. Dead-lettering is **at-most-once**: a failure publishes to the DLQ
immediately and the original message is gone regardless.

```java
@NatsListener(subject = "orders.placed", deadLetterSubject = "orders.placed.dlq")
public void onOrder(Order order) { ... }
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
public void onOrder(Order order) { ... }
```

| Failure type                | Behaviour                                                                                                 |
|-----------------------------|-----------------------------------------------------------------------------------------------------------|
| Argument resolution failure | Message published to DLQ immediately, then `term()`-ed. Retrying a malformed payload would never succeed. |
| Handler invocation failure  | Message is `nak()`-ed and redelivered up to `maxDeliveries` times, then published to DLQ and `term()`-ed. |

If the DLQ publish itself fails, the exception propagates: the message is **not** terminated and will be redelivered.
This may push the delivery count above `maxDeliveries`, which is intentional - the message is retried until the DLQ
becomes reachable rather than being lost.

### Dead-letter headers

Every dead-letter message carries the following headers in addition to all headers from the original message:

| Header                    | Present for    | Value                                                          |
|---------------------------|----------------|----------------------------------------------------------------|
| `X-Dead-Letter-Subject`   | Both           | Original subject the message was received on                   |
| `X-Dead-Letter-Reason`    | Both           | Exception simple name and message, truncated to 200 characters |
| `X-Dead-Letter-Exception` | Both           | Fully-qualified exception class name                           |
| `X-Dead-Letter-Timestamp` | Both           | ISO-8601 UTC timestamp of the dead-letter publish              |
| `X-Dead-Letter-Stream`    | JetStream only | JetStream stream name                                          |
| `X-Dead-Letter-Durable`   | JetStream only | Durable consumer name                                          |
| `X-Dead-Letter-Delivery`  | JetStream only | Delivery count at the time of dead-lettering                   |

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

#### `@NatsPayload`

Marks a parameter explicitly as the message payload. If the type is easily distinguishable (`byte[]`, `String` or a POJO
class/record), the annotation can be omitted and the parameter will be resolved as payload by default. Recommended
keeping for clarity and/or documentation purposes.

```java
@NatsListener(subject = "raw.events")
public void handle(@NatsPayload byte[] body) {}

@NatsListener(subject = "text.events")
public void handle(@NatsPayload String text) {}

@NatsListener(subject = "json.events")
public void handle(@NatsPayload List<Event> events) {}
```

#### `@NatsHeader`

Injects a header value by name. Resolved as `String` (first value), `List<String>`, or `String[]` (all values) depending
on the parameter type.

```java
@NatsListener(subject = "events")
public void handle(Event event, @NatsHeader("X-Correlation-Id") String correlationId) {}

@NatsListener(subject = "events")
public void handle(@NatsHeader("X-Tags") List<String> tags) {}

@NatsListener(subject = "events")
public void handle(@NatsHeader("X-Tags") String[] tags) {}
```

`value` and `name` are aliases; either can be used to specify the header name.

#### `@NatsSubject`

Injects the subject the message was published to. Useful when a listener matches a wildcard subject
and needs to inspect the concrete subject at runtime.

```java
@NatsListener(subject = "events.>")
public void handle(Event event, @NatsSubject String subject) {}

@JetStreamListener(subject = "orders.>", stream = "ORDERS", durable = "router")
public void handle(Order order, @NatsSubject String subject) {}
```

#### `@NatsHeaders`

Injects all message headers. Equivalent to declaring `io.nats.client.impl.Headers` as the parameter type, but explicit.

```java
@NatsListener(subject = "events")
public void handle(Event event, @NatsHeaders Headers headers) {}
```

### JSON deserialization

Any parameter not matched by the rules above is deserialized from the message body using Jackson. Full generic type
information is preserved, so `List<Order>`, `Order[]`, and other parameterized types work correctly.

```java
@NatsListener(subject = "batch.orders")
public void onBatch(List<Order> orders) {}

@NatsListener(subject = "batch.orders")
public void onBatch(Order[] orders) {}
```

### No-arg methods

Methods with no parameters are supported. The message is received and discarded.

```java
@NatsListener(subject = "ping")
public void onPing() {}
```

### Mixed parameters

A method may declare any combination of the above in any order.

```java
@JetStreamListener(subject = "orders.>", stream = "ORDERS", durable = "auditor")
public void onOrder(
    Order order, @NatsHeader("X-Source") String source, Headers allHeaders, Message rawMessage) {}

```

## JetStream stream auto-creation

Declare `io.nats.client.api.StreamConfiguration` beans and the auto-configuration will create or update the
corresponding streams on startup, before any listeners are registered.

> [!IMPORTANT]
> Works only if `natsify.auto-stream-creation` is set to `true` (disabled by default).

```java
@Bean
StreamConfiguration ordersStream() {
  return StreamConfiguration.builder().name("ORDERS").subjects("orders.>").build();
}
```

## Publishing messages

`NatsOperations` is auto-configured and available for injection:

```java
@Autowired
NatsOperations natsOperations;

natsOperations.publish("orders.placed", new Order(...));   // serialized to JSON
natsOperations.publish("orders.placed", "plain text");
natsOperations.publish("orders.placed", rawBytes);
```

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
| `nats.connection.events`                     | Counter | `event`     | Connection state-change events                   |
| `nats.connection.errors`                     | Counter | `error`     | Server error strings received                    |
| `nats.connection.exceptions`                 | Counter | `exception` | Client-side exceptions during processing         |
| `nats.connection.slow.consumer.detected`     | Counter | -           | Slow consumer detections                         |
| `nats.connection.message.discarded`          | Counter | -           | Messages discarded due to a full consumer queue  |
| `nats.connection.pings`                      | Gauge   | -           | Total pings sent                                 |
| `nats.connection.reconnects`                 | Gauge   | -           | Total reconnect attempts                         |
| `nats.connection.in.msgs`                    | Gauge   | -           | Total inbound messages                           |
| `nats.connection.out.msgs`                   | Gauge   | -           | Total outbound messages                          |
| `nats.connection.in.bytes`                   | Gauge   | -           | Total inbound bytes                              |
| `nats.connection.out.bytes`                  | Gauge   | -           | Total outbound bytes                             |
| `nats.connection.dropped.count`              | Gauge   | -           | Messages dropped across all slow consumers       |
| `nats.connection.flush.counter`              | Gauge   | -           | Outgoing message flushes                         |
| `nats.connection.outstanding.requests`       | Gauge   | -           | Outstanding request count                        |
| `nats.connection.oks`                        | Gauge   | -           | Op `+OK` messages received                       |
| `nats.connection.errs`                       | Gauge   | -           | Op `-ERR` messages received                      |
| `nats.connection.requests.sent`              | Gauge   | -           | Requests sent                                    |
| `nats.connection.replies.received`           | Gauge   | -           | Replies received                                 |
| `nats.connection.duplicate.replies.received` | Gauge   | -           | Duplicate replies received (advanced stats only) |
| `nats.connection.orphan.replies.received`    | Gauge   | -           | Orphan replies received (advanced stats only)    |

</details>

## Testing

Add `natsify-starter-test` to your test dependencies:

```xml
<dependency>
    <groupId>io.github.malczuuu.natsify</groupId>
    <artifactId>natsify-starter-test</artifactId>
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
    <version>1.1.3</version>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
class MyIntegrationTests {

  @Container @ServiceConnection
  public static final NatsContainer nats = new NatsContainer("nats:2.14.1");
}
```

`@ServiceConnection` auto-configures `natsify.server` from the running container - no manual property overrides needed.

[nats-testcontainers-java]: https://github.com/AmadeusITGroup/nats-testcontainers-java
