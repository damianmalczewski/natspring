# Listener Dead-Letter Example

Dead-letter pattern with `@NatsListener`. The primary listener intentionally throws an exception on every message to
simulate a handler failure; Natspring routes the failed message to the configured dead-letter subject, where a second
listener captures it.

## Listeners

```
telemetry.>  (core NATS, at-most-once)
      │
      └> onRecord(@NatsListener, deadLetterSubject="dlq.telemetry") - always throws -> dead-lettered

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

## REST API

| Method | Path            | Description                                            |
|--------|-----------------|--------------------------------------------------------|
| GET    | `/dead-letters` | All captured dead-letter messages with body + headers. |

## Running locally

```bash
./gradlew :examples:example-listener-deadletter:bootRun
```

Publish a message (will be dead-lettered immediately):

```bash
nats pub telemetry.temperature '{"bn":"sensor-1","bt":1700000000.0,"n":"temperature","v":23.5,"u":"Cel"}'
```

Query the API:

```bash
curl http://localhost:8081/dead-letters
```
