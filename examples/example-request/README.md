# Request/Reply Example

Request/reply pattern using `NatsOperations.request()` and `@NatsListener` return values. The same application acts as
both the requester (REST endpoints) and the responder (NATS listeners), so no external subscriber is needed.

## Flow

```
GET /add?a=&b=  -->  NatsOperations.request("calc.add", MathRequest)
                          │
                          └> handleAdd(@NatsListener) - returns MathResult  -->  response

GET /echo?text=  -->  NatsOperations.request("calc.echo", String)
                          │
                          └> handleEcho(@NatsListener) - returns text  -->  response
```

## REST API

| Method | Path    | Params   | Description                                        |
|--------|---------|----------|----------------------------------------------------|
| GET    | `/add`  | `a`, `b` | Returns `{"sum": a+b}` via NATS request/reply.     |
| GET    | `/echo` | `text`   | Returns `text` echoed back via NATS request/reply. |

## Running locally

```bash
./gradlew :examples:example-request:bootRun
```

Call the endpoints:

```bash
curl "http://localhost:8080/add?a=3&b=4"
curl "http://localhost:8080/echo?text=hello"
```
