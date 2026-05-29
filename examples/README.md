# Examples

Runnable Spring Boot applications demonstrating Natspring features. All examples use a telemetry subject pattern
(`telemetry.>`) with an example payload and expose a REST endpoint to inspect received messages.

Each example requires a running NATS server (default: `nats://localhost:4222`).

## `example-event-driven-telemetry`

An end-to-end IoT telemetry pipeline combining JetStream fan-out, dead-lettering, MongoDB persistence, and a REST API.

Raw device events arrive on a single entry subject (`iot.events.raw`), are validated by `DeviceEventEntrypointListener`
and forwarded to `iot.events.processed`, where three independent consumers run in parallel. See its own
[`README.md`](example-event-driven-telemetry/README.md) for more details.

## `example-listener`

Demonstrates a basic core NATS listener using `@NatsListener` with a wildcard subject pattern. Received messages are
collected in memory and exposed via `GET /telemetry`.

## `example-jetstream`

Demonstrates a JetStream listener using `@JetStreamListener` with a durable consumer bound to a persistent stream. Shows
automatic stream creation (`auto-stream-creation: true`) and guaranteed at-least-once message delivery.

## `example-listener-deadletter`

Demonstrates the dead-letter pattern with `@NatsListener`. The primary listener intentionally throws an exception to
simulate a handler failure; failed messages are routed to a dead-letter subject and captured by a second listener, whose
collected messages are exposed via `GET /dead-letters`.

## `example-jetstream-deadletter`

Demonstrates dead-letter handling with `@JetStreamListener`. Combines JetStream durability with a configurable
`maxDeliveries` limit - messages that exhaust redelivery attempts are routed to a dead-letter subject and captured by a
core NATS listener, exposed via `GET /dead-letters`.
