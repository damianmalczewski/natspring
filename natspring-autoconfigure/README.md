# Natspring Auto-Configure

[![Sonatype](https://img.shields.io/maven-central/v/io.github.malczuuu.natspring/natspring-autoconfigure)][maven-central]

Spring Boot auto-configuration module that wires `natspring-core` into a Spring application context. Provides
`NatsProperties` for connection configuration, auto-configured optional health indicators, and optional Micrometer
metrics. Also includes Testcontainers integration for spinning up a NATS container in tests.

## Dependency

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-autoconfigure</artifactId>
    <version>{version}</version>
</dependency>
```

```kotlin
dependencies {
    implementation("io.github.malczuuu.natspring:natspring-autoconfigure:{version}")
}
```

[maven-central]: https://central.sonatype.com/artifact/io.github.malczuuu.natspring/natspring-autoconfigure
