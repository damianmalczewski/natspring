import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.springframework.boot")
    id("internal.jacoco-convention")
    id("com.diffplug.spotless")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.namastack.outbox.bom))

    implementation(project(":natspring-starter"))
    implementation(project(":natspring-namastack-outbox"))
    implementation(libs.namastack.outbox.starter.jpa)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.webmvc)
    runtimeOnly(libs.flyway.database.postgresql)

    runtimeOnly(libs.postgresql)

    testImplementation(project(":natspring-starter-test"))
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.starter.flyway.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)
    testImplementation(libs.testcontainers.postgresql)

    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    if (name != "bootJar") {
        enabled = false
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("build/**")

        ktlint("1.8.0")
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    sql {
        target("src/**/*.sql")
        targetExclude("build/**")

        dbeaver()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("yaml") {
        target("src/**/*.yml", "src/**/*.yaml")
        targetExclude("build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}
