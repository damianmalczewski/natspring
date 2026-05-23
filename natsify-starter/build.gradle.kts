plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(libs.spring.boot.starter)
    api(libs.spring.boot.starter.jackson)
    api(project(":natsify-autoconfigure"))
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natsify Starter"
    description = "Spring Boot Starter Module of Natsify Project"
}
