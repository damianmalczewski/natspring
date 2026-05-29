plugins {
    alias(libs.plugins.nmcp)
    id("java-platform")
    id("internal.publishing-convention")
}

dependencies {
    constraints {
        api(project(":natspring-autoconfigure"))
        api(project(":natspring-core"))
        api(project(":natspring-namastack-outbox"))
        api(project(":natspring-starter"))
        api(project(":natspring-starter-test"))
        api(libs.jackson.databind)
        api(libs.jnats)
        api(libs.jspecify)
        api(libs.micrometer.core)
        api(libs.slf4j.api)
        api(libs.testcontainers.nats)
    }
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring BOM"
    description = "BOM Module of Natspring Project"
}
