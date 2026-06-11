plugins {
    alias(libs.plugins.nmcp)
    id("internal.java-library-convention")
    id("internal.publishing-convention")
}

dependencies {
    api(project(":natspring-starter"))
    api(libs.spring.boot.starter.test)
    api(libs.spring.boot.starter.jackson.test)
}

// see build-logic/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Starter Test"
    description = "Spring Boot Starter Test Module of Natspring Project"
}
