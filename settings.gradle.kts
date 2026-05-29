pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "natspring"

include(":natspring-autoconfigure")
include(":natspring-core")
include(":natspring-integration-tests")
include(":natspring-starter")
include(":natspring-starter-test")
include(":examples:example-event-driven-telemetry")
include(":examples:example-jetstream")
include(":examples:example-jetstream-deadletter")
include(":examples:example-listener")
include(":examples:example-listener-deadletter")
include(":examples:example-request")
