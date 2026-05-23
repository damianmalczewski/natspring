pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "natsify"

include(":natsify-autoconfigure")
include(":natsify-core")
include(":natsify-integration-tests")
include(":natsify-starter")
include(":natsify-starter-test")
include(":examples:example-common")
include(":examples:example-jetstream")
include(":examples:example-listener")
