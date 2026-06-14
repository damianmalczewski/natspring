plugins {
    alias(libs.plugins.nmcp)
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
}

dependencies {
    api(libs.jnats)
    api(libs.jspecify)
    api(libs.spring.context)

    compileOnly(libs.gson)
    compileOnly(libs.jackson.databind)
    compileOnly(libs.micrometer.core)
    compileOnly(libs.slf4j.api)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.gson)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.micrometer.core)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Core"
    description = "Core Module of Natspring Project"
}
