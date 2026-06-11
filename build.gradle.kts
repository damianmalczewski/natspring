import com.diffplug.spotless.LineEnding

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.nmcp) apply false
    alias(libs.plugins.nmcp.aggregation)
    id("internal.idea-convention")
    id("jacoco-report-aggregation")
    id("test-report-aggregation")
}

dependencies {
    nmcpAggregation(project(":natspring-autoconfigure"))
    nmcpAggregation(project(":natspring-bom"))
    nmcpAggregation(project(":natspring-core"))
    nmcpAggregation(project(":natspring-starter"))
    nmcpAggregation(project(":natspring-starter-test"))

    jacocoAggregation(project(":natspring-autoconfigure"))
    jacocoAggregation(project(":natspring-core"))
    jacocoAggregation(project(":natspring-integration-tests"))
    jacocoAggregation(project(":natspring-starter"))
    jacocoAggregation(project(":natspring-starter-test"))

    testReportAggregation(project(":natspring-autoconfigure"))
    testReportAggregation(project(":natspring-core"))
    testReportAggregation(project(":natspring-integration-tests"))
    testReportAggregation(project(":natspring-starter"))
    testReportAggregation(project(":natspring-starter-test"))
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("PUBLISHING_USERNAME")
        password = System.getenv("PUBLISHING_PASSWORD")

        publishingType = "USER_MANAGED"
    }
}

reporting {
    reports {
        register<JacocoCoverageReport>("testCodeCoverageReport") {
            testSuiteName = "test"
        }
        register<AggregateTestReport>("testAggregateTestReport") {
            testSuiteName = "test"
        }
    }
}

spotless {
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")

        ktlint("1.8.0").editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("misc") {
        target("**/.gitattributes", "**/.gitignore")
        targetExclude("**/build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "io/github/malczuuu/natspring/autoconfigure/NatsProperties.class",
                        "io/github/malczuuu/natspring/instrument/JetStreamListenerObserver.class",
                        "io/github/malczuuu/natspring/instrument/NatsConnectionObserver.class",
                        "io/github/malczuuu/natspring/instrument/NatsListenerObserver.class",
                        "**/*Exception.class",
                    )
                }
            },
        ),
    )
}

tasks.named<Task>("check") {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}

defaultTasks("spotlessApply", "build")
