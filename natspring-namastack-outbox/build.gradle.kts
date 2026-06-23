plugins {
    id("com.gradleup.nmcp")
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.kotlin-library-convention")
    id("internal.publishing-convention")
}

dependencies {
    api(project(":natspring-autoconfigure"))
    api(libs.namastack.outbox.api)

    compileOnly(libs.slf4j.api)

    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.test)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Namastack Outbox"
    description = "Namastack Outbox Integration Module of Natspring Project"
}
