# Namastack Outbox Example

Transactional outbox pattern using [Namastack Outbox](https://github.com/namastack/namastack-outbox) with NATS
JetStream as the message broker. Demonstrates how placing an order in a single transaction also schedules an outbox
event that is reliably published to NATS after the transaction commits.

## Flow

```
OrderService.placeOrder()
      │
      ├── saves Order to PostgreSQL
      └── schedules OrderCreatedEvent via Outbox (same transaction)
                │
                └──> orders.events  (orders stream, durable=order-events-consumer)
                           │
                           └> OrderEventListener.onOrderCreated(@JetStreamListener)
```

The `orders` stream is created automatically on startup via a `StreamConfiguration` bean with
`auto-stream-creation: true`.

## REST API

| Method | Path      | Description   |
|--------|-----------|---------------|
| POST   | `/orders` | Place an order |

## Running locally

Start NATS and PostgreSQL:

```bash
docker compose up -d
```

Run the application:

```bash
./gradlew :examples:example-namastack-outbox:bootRun
```

Subscribe to order events:

```bash
nats sub 'orders.events'
```

Place an order:

```bash
curl -s -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerName": "John Doe"}' | jq
```
