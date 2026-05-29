plugins {
    id("internal.java-convention")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(libs.spring.boot.starter.webmvc)
    implementation(project(":natspring-starter"))

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Jar>().configureEach {
    if (name != "bootJar") {
        enabled = false
    }
}
