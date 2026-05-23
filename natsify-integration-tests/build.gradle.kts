plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-convention")
}

dependencies {
    // Main
    implementation(project(":natsify-starter"))

    // Test
    testImplementation(project(":natsify-starter-test"))
    testImplementation(libs.spring.boot.starter.micrometer.metrics.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}
