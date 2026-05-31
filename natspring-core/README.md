# Natspring Core

[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-core)][maven-central]

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

Startup is driven by three `SmartLifecycle` beans started in phase order:

```txt
  Spring context startup
          │
          ▼
  ManagedConnectionLifecycle   ← phase N   (ConnectionLifecycle extends Connection)
          │ establishes NATS connection
          ▼
  ManagedJetStreamLifecycle    ← phase N+50  (creates/updates JetStream streams)
          │
          ▼
  ManagedListenerContainerLifecycle  ← phase N+100
          │ starts
          ├──────────────────────────────────────────┐
          ▼                                          ▼
  NatsMessageListenerContainer        JetStreamMessageListenerContainer
      reads                               reads
  NatsListenerEndpointRegistry        JetStreamListenerEndpointRegistry
    (populated by BPP)                  (populated by BPP)
          │                                          │
          │                             ┌────────────┴────────────┐
          │                             │                         │
          ▼                             ▼                         ▼
  SubscriptionHandler          JetStreamPushHandler     JetStreamPullHandler
          │                             └────────────┬────────────┘
          │                                          │
          ▼                                          ▼
  NatsListenerInvocation                 JetStreamInvocation
          │                                          │
          └──────────────────┬───────────────────────┘
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

`ManagedConnectionLifecycle` implements `Connection` directly - it is the `Connection` bean injected throughout the application. `ConnectionWatcher` implements `ConnectionListener` and `ErrorListener` to bridge raw NATS connection/error events to `NatsConnectionObserver`.

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

[maven-central]: https://central.sonatype.com/artifact/io.github.malczuuu.natspring/natspring-core
