# Natspring Namastack Outbox

Spring Boot auto-configuration that bridges [Namastack Outbox](https://github.com/namastack/namastack-outbox) with NATS.
Registers a `NatsOutboxHandler` bean that picks up outbox records and publishes them to the configured NATS subject.

> [!IMPORTANT]
> This module is an experiment and may go away in the future.

## Dependency

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-namastack-outbox</artifactId>
    <version>{version}</version>
</dependency>
```

```kotlin
dependencies {
    implementation("io.github.malczuuu.natspring:natspring-namastack-outbox:{version}")
}
```

Note that `natspring-starter` must be included separately.

## Configuration

```yaml
natspring:
  namastack:
    outbox:
      enabled: true          # default
      default-subject: events
```

A custom `NatsOutboxRouting` bean disables the auto-configured routing, allowing full control over subject resolution,
key extraction, header injection, payload mapping, and filtering per payload type.

See the [example](../examples/example-namastack-outbox) for a complete working setup.
