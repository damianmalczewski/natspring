plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(libs.spring.boot.starter.jackson)
    api(project(":natspring-autoconfigure"))
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Starter"
    description = "Spring Boot Starter Module of Natspring Project"
}
