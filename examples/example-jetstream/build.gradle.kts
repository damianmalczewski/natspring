import com.diffplug.spotless.LineEnding

plugins {
    id("java")
    id("com.diffplug.spotless")
    id("org.springframework.boot")
    id("internal.jacoco-convention")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(project(":natspring-starter"))
    implementation(libs.spring.boot.starter.webmvc)

    testImplementation(project(":natspring-starter-test"))
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.nats)

    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
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
    java {
        target("src/**/*.java")
        targetExclude("build/**")

        googleJavaFormat("1.28.0")
        forbidWildcardImports()
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
