plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-convention")
    id("internal.java-spotless-convention")
}

dependencies {
    implementation(project(":natspring-starter"))
    implementation(libs.spring.boot.micrometer.metrics)

    runtimeOnly(libs.micrometer.registry.prometheus)

    testImplementation(project(":natspring-starter-test"))
    testImplementation(libs.spring.boot.health)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}
