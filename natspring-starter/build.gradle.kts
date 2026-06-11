plugins {
    alias(libs.plugins.nmcp)
    id("internal.java-library-convention")
    id("internal.publishing-convention")
}

dependencies {
    api(project(":natspring-autoconfigure"))
    api(libs.spring.boot.starter)
    api(libs.spring.boot.starter.jackson)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Starter"
    description = "Spring Boot Starter Module of Natspring Project"
}
