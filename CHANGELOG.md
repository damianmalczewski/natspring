# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Add `natsify-bom` module for version management.

### Changed

- Deprecate `@NatsHeader.name()` - let's rely only on `@NatsHeader.value()`.

## [0.1.1] - 2026-05-30

### Fixed

- Add `isEmpty` to `MessageListenerContainer` and hide unnecessary logging.
- Do not relate `deadLetterDeliveries` with `maxDeliver`, as dead-lettering is an opinionated feature coming from
  Natspring Project and not a core NATS concept.

## [0.1.0] - 2026-05-30

### Added

- Add support for managing NATS connection lifecycle.
- Add support for JetStream stream creation on application startup.
- Add `@NatsListener` and `@JetStreamListener` annotations post-processor for automatic listener registration.
- Add dead-letter topic support for both listener types (with at-most-once QoS for `@NatsListener`).
- Add `NatsOperations` for common publish and request/reply operations.
- Add support for interceptors for annotation-based listeners and `NatsOperations`.
- Add support for `micrometer-metrics` for NATS metrics.
- Add support for `spring-boot-health` for `NatsHealthIndicator`.
- Add Testcontainers support via [`io.github.amadeusitgroup.testcontainers:nats`][nats-testcontainers-java] library and
  `@ServiceConnection` for automatic service connection management.
- Add Spring Boot starter modules for easy integration.
- Add a bunch of examples to showcase the features.

[nats-testcontainers-java]: https://github.com/AmadeusITGroup/nats-testcontainers-java

[keepachangelog]: https://keepachangelog.com/en/1.1.0/

[semver]: https://semver.org/spec/v2.0.0.html
