# JetStream Dead-Letter Example

Dead-letter handling with `@JetStreamListener`. Combines JetStream durability with a configurable `maxDeliveries`limit,
so messages that exhaust redelivery attempts are routed to a dead-letter subject and captured by a core NATS listener.

## Listeners

```
telemetry.>  (TELEMETRY stream, durable=telemetry-dlq-listener, maxDeliveries=1, at-least-once)
      │
      └> onRecord(@JetStreamListener, deadLetterSubject="dlq.telemetry") - always throws -> dead-lettered after 1 attempt

dlq.telemetry  (core NATS, at-most-once)
      │
      └> onDeadLetter(@NatsListener) - captures raw Message with headers, appends to in-memory list
```

Dead-letter messages carry diagnostic headers set by Natspring:

| Header                      | Content                                    |
|-----------------------------|--------------------------------------------|
| `X-Dead-Letter-Subject`     | Original subject the message was sent to.  |
| `X-Dead-Letter-Reason`      | Exception message.                         |
| `X-Dead-Letter-Exception`   | Fully-qualified exception class name.      |
| `X-Dead-Letter-Timestamp`   | ISO-8601 timestamp of the failure.         |
| `X-Dead-Letter-Stream`      | JetStream stream name.                     |
| `X-Dead-Letter-Durable`     | Durable consumer name.                     |
| `X-Dead-Letter-Delivery`    | Delivery attempt count at time of failure. |

## REST API

| Method | Path            | Description                                            |
|--------|-----------------|--------------------------------------------------------|
| GET    | `/dead-letters` | All captured dead-letter messages with body + headers. |

## Running locally

```bash
./gradlew :examples:example-jetstream-deadletter:bootRun
```

Publish a message (will be dead-lettered after 1 delivery attempt):

```bash
nats pub telemetry.temperature '{"bn":"sensor-1","bt":1700000000.0,"n":"temperature","v":23.5,"u":"Cel"}'
```

Query the API:

```bash
curl http://localhost:8081/dead-letters
```
