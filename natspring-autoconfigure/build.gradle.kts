plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(libs.spring.boot.autoconfigure)
    api(project(":natspring-core"))

    compileOnly(libs.spring.boot.health)
    compileOnly(libs.spring.boot.testcontainers)
    compileOnly(libs.jackson.databind)
    compileOnly(libs.micrometer.core)
    compileOnly(libs.testcontainers.nats)

    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.health)
    testImplementation(libs.spring.boot.jackson)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Auto-Configure"
    description = "Spring Boot Auto-Configure Module of Natspring Project"
}
