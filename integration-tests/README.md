# Integration Tests

End-to-end integration tests for Natspring running against a real NATS server. Uses Testcontainers to start a NATS
instance automatically, then exercises core and autoconfigure via a full Spring Boot application context. Tests here
cover both core NATS and JetStream scenarios.

This module does not produce an artifact and is not intended for use outside the Natspring project.
