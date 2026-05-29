# Event-Driven Telemetry Example

Event-driven IoT telemetry pipeline using JetStream, MongoDB, and a REST API.

## Architecture

```
iot.events.raw  (IOT_RAW stream, maxDeliveries=5)
      │
      └> DeviceEventEntrypointListener (@JetStreamListener, durable=iot-raw-processor) - validates, publishes

iot.events.processed  (IOT_PROCESSED stream)
      │
      ├─> DeviceEventPersistListener (@JetStreamListener, durable=iot-event-persister)
      │     -> upserts DeviceEventDocument by eventId
      │
      ├─> DeviceEventCounterListener (@JetStreamListener, durable=iot-device-counter)
      │     -> $inc totalEvents on DeviceInfoDocument
      │
      └─< DeviceActivityListener (@NatsListener) - at-most-once
            -> $set lastActivityAt on DeviceInfoDocument

iot.events.deadletter  (IOT_DLQ stream)
      │
      └─> DeadLetterListener (@JetStreamListener, durable=iot-dlq-persister)
            -> upserts DeadLetterDocument by streamId
```

Messages published to `iot.events.raw` are validated and forwarded to `iot.events.processed`. Invalid messages are
nacked and retried up to 5 times, then dead-lettered to `iot.events.deadletter`.

`iot.events.processed` is consumed by three independent consumers:

- **`DeviceEventPersistListener`** - JetStream, at-least-once, persists the full event to MongoDB
- **`DeviceEventCounterListener`** - JetStream, at-least-once, increments the per-device event counter
- **`DeviceActivityListener`** - core NATS, at-most-once, updates the last activity timestamp

> **`DeviceActivityListener`** uses core NATS deliberately: last-seen is a best-effort metric. Messages published while
> the service is down are not replayed, so `lastActivityAt` may lag behind the true last event time after a restart.

## Message IDs and Idempotency

Event and dead-letter documents are keyed by `{crc32(streamName)}-{paddedSequence}` (e.g. `a1b2c3d4-000000000042`). The
stream name is hashed with CRC32 to avoid leaking internal infrastructure names through the API. The sequence is
zero-padded to 12 digits for natural sort order.

JetStream sequences are monotonically increasing and never reset for the lifetime of the stream, so this ID is:

- **Unique** - no two messages in the same stream share a sequence number
- **Idempotent** - redeliveries carry the same sequence, so a MongoDB upsert by this ID is a no-op

> **Caveat**: if a stream is deleted and recreated, sequences restart from 1 and the guarantee
> breaks. In production, never delete streams - use purge instead.

## REST API

| Method | Path                                           | Description                                                           |
|--------|------------------------------------------------|-----------------------------------------------------------------------|
| GET    | `/api/v1/devices/{id}`                         | Device metadata (totalEvents, lastActivityAt). 404 if unknown.        |
| GET    | `/api/v1/devices/{id}/history-events`          | Full event history as `{"content": []}`.                              |
| GET    | `/api/v1/management/dead-letter-messages`      | Dead-letter list as `{"content": []}`. Payload truncated to 32 chars. |
| GET    | `/api/v1/management/dead-letter-messages/{id}` | Dead-letter detail with full payload. 404 if unknown.                 |

## Running locally

Start NATS and MongoDB via Docker Compose:

```bash
docker compose up -d
```

```bash
./gradlew :examples:example-event-driven-telemetry:bootRun
```

Publish a message:

```bash
# valid
nats pub iot.events.raw '{"deviceId":"dev-1","type":"temperature","payload":{"value":23.5},"timestamp":"2026-05-29T10:00:00Z"}'

# invalid (blank deviceId -> retried 5x -> dead-lettered)
nats pub iot.events.raw '{"deviceId":"","type":"temperature","payload":{},"timestamp":"2026-05-29T10:00:00Z"}'
```

Query the API:

```bash
curl http://localhost:8081/api/v1/devices/dev-1
curl http://localhost:8081/api/v1/devices/dev-1/history-events
curl http://localhost:8081/api/v1/management/dead-letter-messages
```

## MongoDB collections

| Collection      | Upsert key | Description                                        |
|-----------------|------------|----------------------------------------------------|
| `device_events` | `eventId`  | Persisted IoT events                               |
| `device_infos`  | `deviceId` | Per-device totalEvents + lastActivityAt (upserted) |
| `dead_letters`  | `streamId` | Failed messages with DLQ headers                   |
