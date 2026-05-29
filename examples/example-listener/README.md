# Listener Example

Basic core NATS listener using `@NatsListener` with a wildcard subject pattern. Received messages are collected in
memory and exposed via REST.

## Listener

```
telemetry.>  (core NATS, at-most-once)
      │
      └> NatsListenerExample.onRecord(@NatsListener) - deserializes SenmlRecord, appends to in-memory list
```

## REST API

| Method | Path         | Description                                   |
|--------|--------------|-----------------------------------------------|
| GET    | `/telemetry` | All received SenML records as a JSON array.   |

## Running locally

```bash
./gradlew :examples:example-listener:bootRun
```

Publish a message:

```bash
nats pub telemetry.temperature '{"bn":"sensor-1","bt":1700000000.0,"n":"temperature","v":23.5,"u":"Cel"}'
```

Query the API:

```bash
curl http://localhost:8081/telemetry
```
