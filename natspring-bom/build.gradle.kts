plugins {
    id("com.gradleup.nmcp")
    id("internal.java-platform-convention")
    id("internal.publishing-convention")
}

dependencies {
    constraints {
        api(project(":natspring-autoconfigure"))
        api(project(":natspring-core"))
        api(project(":natspring-starter"))
        api(project(":natspring-starter-test"))
        api(libs.testcontainers.nats)
    }
}

// see buildSrc/src/main/kotlin/internal.publishing-convention.gradle.kts
internalPublishing {
    displayName = "Natspring BOM"
    description = "BOM Module of Natspring Project"
}
