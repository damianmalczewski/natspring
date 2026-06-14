# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Allow configuring the behavior for failures in method argument resolution (mostly deserialization) for
  `@JetStreamListener`-annotated methods.
- Allow `@NatsHeader`-annotated parameter without `value` attribute (fallback to method argument name).
- Add `NatsClient` interface with package-private implementation.

### Changed

- Deprecate `NatsOperations` and `NatsTemplate` in favor of `NatsClient`.

## [0.3.0] - 2026-06-13

### Breaking

- Remove `@NatsHeader.name()` - use `@NatsHeader.value()` instead.
- Remove `ConnectionException` - use `NatsConnectionException` instead.
- Remove `ListenerConfigureException` - use `NatsListenerMethodException` instead.
- Remove `NatsIntegrationException` - use `NatsMessagingException` instead.
- Remove `StreamConfigureException` - use `JetStreamConfigureException` instead.

### Added

- Add `NatsMessageConverter` interface - abstraction for NATS message serialization and deserialization.
- Hide away JSON serdes with `NatsMessageConverter` and `JacksonNatsMessageConverter` for Jackson.
- Add `natspring-namastack-outbox` module - transactional outbox.

### Changed

- Jackson exceptions now wrapped in `NatsMessageConversionException` instead of propagating raw.

## [0.2.0] - 2026-06-02

### Added

- Add `natspring-bom` module for version management.
- Add `ConnectionHook` to allow users to hook into the NATS connection lifecycle.

### Changed

- Deprecate `@NatsHeader.name()` - let's rely only on `@NatsHeader.value()`.
- Exclude internal components from injecting via `autowireCandidate = false`.

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
