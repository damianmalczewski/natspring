# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Add support for managing NATS connection lifecycle.
- Add `@NatsListener` and `@JetStreamListener` annotations post-processor for automatic listener registration.
- Add `NatsOperations` for common publish operations.
- Add support for `micrometer-metrics` for NATS metrics.
- Add support for `spring-boot-health` for `NatsHealthIndicator`.
- Add Testcontainers support via [`io.github.amadeusitgroup.testcontainers:nats`][nats-testcontainers-java] library and
  `@ServiceConnection` for automatic service connection management.
- Add Spring Boot starter modules for easy integration.

[nats-testcontainers-java]: https://github.com/AmadeusITGroup/nats-testcontainers-java

[keepachangelog]: https://keepachangelog.com/en/1.1.0/

[semver]: https://semver.org/spec/v2.0.0.html
