# Natspring Starter

Convenience starter for adding Natspring to a Spring Boot application. Pulls in `natspring-autoconfigure`, the Spring Boot
starter, and Jackson in a single dependency. Add this to your project instead of depending on core and autoconfigure
separately.

## Dependency

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-starter</artifactId>
    <version>{version}</version>
</dependency>
```

```kotlin
dependencies {
    implementation("io.github.malczuuu.natspring:natspring-starter:{version}")
}
```
