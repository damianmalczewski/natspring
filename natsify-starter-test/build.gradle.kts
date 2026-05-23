plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(libs.spring.boot.starter.test)
    api(libs.spring.boot.starter.jackson.test)
    api(project(":natsify-starter"))
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natsify Starter Test"
    description = "Spring Boot Starter Test Module of Natsify Project"
}
