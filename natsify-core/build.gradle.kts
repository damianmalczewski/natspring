plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    // Main
    api(libs.jnats)
    api(libs.jspecify)
    api(libs.spring.context)

    compileOnly(libs.jackson.databind)
    compileOnly(libs.micrometer.core)
    compileOnly(libs.slf4j.api)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.micrometer.core)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natsify Core"
    description = "Core Module of Natsify Project"
}
