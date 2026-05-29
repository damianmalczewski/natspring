plugins {
    id("internal.errorprone-convention")
    id("internal.jacoco-convention")
    id("internal.java-convention")
}

dependencies {
    implementation(project(":natspring-starter"))

    testImplementation(project(":natspring-starter-test"))
    testImplementation(libs.spring.boot.starter.micrometer.metrics.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}
