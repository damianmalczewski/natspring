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
- [Method parameter types](#method-parameter-types)
    - [Parameter annotations](#parameter-annotations)
        - [`@NatsPayload`](#natspayload)
        - [`@NatsHeader`](#natsheader)
        - [`@NatsHeaders`](#natsheaders)
    - [JSON deserialization](#json-deserialization)
    - [No-arg methods](#no-arg-methods)
    - [Mixed parameters](#mixed-parameters)
- [JetStream stream auto-creation](#jetstream-stream-auto-creation)
- [Publishing messages](#publishing-messages)
- [Testing](#testing)
    - [Testcontainers](#testcontainers)

## Configuration

Following properties are supported, with defaults shown:

```properties
natsify.server=nats://localhost:4222
natsify.username=
natsify.password=
natsify.auto-stream-create=false
```

## Listener annotations

### `@NatsListener`

Subscribes to a core NATS subject. Supports queue groups for load balancing.

```java
@NatsListener(subject = "orders.placed")
public void onOrder(Order order) {}

@NatsListener(subject = "orders.placed", group = "order-processors")
public void onOrderQueued(Order order) {}
```

| Attribute | Description                                                                                  |
|-----------|----------------------------------------------------------------------------------------------|
| `subject` | NATS subject pattern (wildcards `*` and `>` supported). Supports `${property}` placeholders. |
| `queue`   | Optional queue group name for competing-consumer load balancing.                             |

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

| Attribute | Description                                                                       |
|-----------|-----------------------------------------------------------------------------------|
| `subject` | Subject pattern to filter within the stream. Supports `${property}` placeholders. |
| `stream`  | JetStream stream name. Optional; NATS will infer from the subject if omitted.     |
| `durable` | Durable consumer name. Omit for an ephemeral consumer.                            |
| `queue`   | Optional queue group name for competing-consumer load balancing.                  |

## Method parameter types

Parameters are resolved in the order listed below. The first match wins.

| Priority | Condition                                                                | Resolved value                                             |
|----------|--------------------------------------------------------------------------|------------------------------------------------------------|
| 1        | Parameter type is `io.nats.client.Message` (or subtype)                  | Raw NATS message                                           |
| 2        | Parameter annotated with `@NatsHeader`                                   | Header value(s) as `String`, `List<String>`, or `String[]` |
| 3        | Parameter annotated with `@NatsHeaders`                                  | All headers as `io.nats.client.impl.Headers`               |
| 4        | Parameter type is `io.nats.client.impl.Headers` (without `@NatsPayload`) | All headers as `io.nats.client.impl.Headers`               |
| 5        | Parameter type is `byte[]`                                               | Raw message body bytes                                     |
| 6        | Parameter type is `String`                                               | Message body decoded as UTF-8                              |
| 7        | Any other type, or `@NatsPayload`-annotated parameter                    | Message body deserialized from JSON                        |

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
> Works only if `natsify.auto-stream-create` is set to `true` (disabled by default).

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
  static final NatsContainer nats = new NatsContainer("nats:2.14.0");
}
```

`@ServiceConnection` auto-configures `natsify.server` from the running container - no manual property overrides needed.

[nats-testcontainers-java]: https://github.com/AmadeusITGroup/nats-testcontainers-java
