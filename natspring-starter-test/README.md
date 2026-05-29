# Natspring Starter Test

Test utilities starter for Natspring-based applications. Extends `natspring-starter` with Spring Boot Test and Jackson test
support so integration tests have everything they need in one dependency. Intended for use in the `testImplementation`
configuration only.

## Dependency

```xml
<dependency>
    <groupId>io.github.malczuuu.natspring</groupId>
    <artifactId>natspring-starter-test</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

```kotlin
dependencies {
    testImplementation("io.github.malczuuu.natspring:natspring-starter-test:{version}")
}
```
