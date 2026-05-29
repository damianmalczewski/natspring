plugins {
    id("internal.java-library-convention")
    id("internal.publishing-convention")
    alias(libs.plugins.nmcp)
}

dependencies {
    api(libs.spring.boot.starter.jackson.test)
    api(project(":natspring-starter"))
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring Starter Test"
    description = "Spring Boot Starter Test Module of Natspring Project"
}
