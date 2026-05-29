# Natspring Project - Agent Instructions

Spring Boot auto-configuration for NATS. Provides annotation-driven message listeners (`@NatsListener`,
`@JetStreamListener`) and a publishing API (`NatsOperations`) with optional Micrometer instrumentation.

## Build & Validate

- **Always run `./gradlew`** (default tasks: `spotlessApply build`) to format, compile, and test.
- If Spotless fails, run `./gradlew spotlessApply` to auto-fix, then re-run `./gradlew`.
- JDK 25 toolchain required; main sources compile to Java 17 bytecode (`--release 17`).
- Dependencies: `gradle/libs.versions.toml`. Refresh with `./gradlew --refresh-dependencies`.
- Always validate changes with a full `./gradlew` run before considering a task complete.

## Project Layout

| Module                        | Contents                                                   |
|-------------------------------|------------------------------------------------------------|
| `natspring-core`              | Core API, annotation processing, argument resolution       |
| `natspring-autoconfigure`     | Spring Boot auto-configuration and `NatsProperties`        |
| `natspring-starter`           | Convenience starter (depends on core + autoconfigure)      |
| `natspring-starter-test`      | Test utilities starter                                     |
| `natspring-integration-tests` | Full Spring Boot integration tests (requires running NATS) |
| `examples/`                   | Runnable example applications                              |
| `buildSrc/`                   | Internal Gradle convention plugins                         |

## Agent Rules

- Do not use terminal commands (e.g., `cat`, `find`, `ls`) to read or list project files - use IDE/agent tools instead.
- Run tests once, save output to `build/test-run.log` inside the repo (`> build/test-run.log 2>&1`), then read from that
  file to extract errors. Never run the same test command multiple times, without changes in sources. Store test output
  in multiple files if you want to compare before/after changes (ex. `build/test-run-{i}.log`).

## Coding Rules

- No self-explaining comments - only add comments for non-obvious context.
- No wildcard imports.
- Follow existing code patterns and naming conventions.
- Let `spotlessApply` handle all formatting - never format manually.

## Test Conventions

- Method naming: `givenThis_whenThat_thenWhat`.
- No `// given`, `// when`, `// then` section comments.
- Cover both positive and negative cases.
- Use AssertJ for assertions.
