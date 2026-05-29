# JetStream Listener Example

JetStream listener using `@JetStreamListener` with a durable consumer bound to a persistent stream. Demonstrates
automatic stream creation and guaranteed at-least-once message delivery.

## Listener

```
telemetry.>  (TELEMETRY stream, durable=telemetry-listener, at-least-once)
      │
      └> JetStreamListenerExample.onRecord(@JetStreamListener) - deserializes SenmlRecord, appends to in-memory list
```

The `TELEMETRY` stream is created automatically on startup via a `StreamConfiguration` bean with
`auto-stream-creation: true`. Messages published before the application starts are replayed on reconnect.

## REST API

| Method | Path         | Description                                 |
|--------|--------------|---------------------------------------------|
| GET    | `/telemetry` | All received SenML records as a JSON array. |

## Running locally

```bash
./gradlew :examples:example-jetstream:bootRun
```

Publish a message:

```bash
nats pub telemetry.temperature '{"bn":"sensor-1","bt":1700000000.0,"n":"temperature","v":23.5,"u":"Cel"}'
```

Query the API:

```bash
curl http://localhost:8081/telemetry
```
