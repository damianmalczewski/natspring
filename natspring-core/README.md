# Natspring Core

Core module providing the public API for NATS integration. Contains the publishing API (`NatsOperations`,
`NatsTemplate`), listener annotations (`@NatsListener`, `@JetStreamListener`), and argument resolution for annotated
listener methods. Optional Micrometer instrumentation for connections and listeners is also included here.

## Dependency

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-core</artifactId>
    <version>{version}</version>
</dependency>
```

```kotlin
dependencies {
    implementation("io.github.malczuuu.natspring:natspring-core:{version}")
}
```

## Architecture

The general architecture of how listener methods are registered to a NATS connections:

```txt
                   Spring context startup
                            │
                            ▼
            DefaultConnectionManager (SmartLifecycle)
                            │ establishes
                            ▼
                       NATS Connection
                  ┌─────────┴────────────────┐
                  │                          │
                  ▼                          ▼
  NatsMessageListenerContainer     JetStreamMessageListenerContainer
      reads                            reads
  NatsListenerEndpointRegistry     JetStreamListenerEndpointRegistry
    (populated by BPP)               (populated by BPP)
          │                                  │
          │                       ┌──────────┴──────────┐
          │                       │                     │
          ▼                       ▼                     ▼
  SubscriptionHandler      JetStreamPushHandler  JetStreamPullHandler
          │                       └──────────┬───────────┘
          │                                  │
          ▼                                  ▼
  NatsListenerInvocation           JetStreamInvocation
          │                                  │
          └─────────────────┬────────────────┘
                            │
                            ▼
                 MessageArgumentResolver
                resolves method parameters
                from NATS message payload,
                headers, subject, metadata
                            │
                            ▼
          @NatsListener / @JetStreamListener method
```

Bean post-processors scan application beans at startup:

```txt
  @NatsListener method  ──▶  NatsListenerAnnotationBeanPostProcessor
                                      │ registers
                                      ▼
                           NatsListenerEndpointRegistry


  @JetStreamListener method  ──▶  JetStreamListenerAnnotationBeanPostProcessor
                                          │ registers
                                          ▼
                               JetStreamListenerEndpointRegistry
```
