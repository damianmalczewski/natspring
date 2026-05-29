import com.diffplug.spotless.LineEnding
import internal.getBooleanProperty

plugins {
    id("internal.common-convention")
    id("internal.idea-convention")
    id("jacoco-report-aggregation")
    alias(libs.plugins.nmcp).apply(false)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.spotless)
}

dependencies {
    nmcpAggregation(project(":natspring-autoconfigure"))
    nmcpAggregation(project(":natspring-core"))
    nmcpAggregation(project(":natspring-starter"))
    nmcpAggregation(project(":natspring-starter-test"))

    jacocoAggregation(project(":natspring-autoconfigure"))
    jacocoAggregation(project(":natspring-core"))
    jacocoAggregation(project(":natspring-integration-tests"))
    jacocoAggregation(project(":natspring-starter"))
    jacocoAggregation(project(":natspring-starter-test"))
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
    }
}

spotless {
    val licenseHeader = "${rootProject.rootDir}/gradle/license-header.java"
    val updateLicenseYear = project.getBooleanProperty("spotless.license-year-enabled")

    java {
        target("**/src/**/*.java")
        targetExclude("**/build/**")
        licenseHeaderFile(licenseHeader).updateYearWithLatest(updateLicenseYear)

        // NOTE: decided not to upgrade Google Java Format, as versions 1.29+ require running it on Java 21
        googleJavaFormat("1.28.0")
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("javaMisc") {
        target("**/src/**/package-info.java", "**/src/**/module-info.java")
        targetExclude("**/build/**")

        // License headers in these files are not formatted with standard java group, so we need to use custom settings.
        // The regex is designed to find out where the code starts in these files, so the license header can be placed
        // before it.
        //
        // The code starts with either:
        //
        // - any annotation (ex. @NullMarked before package declaration),
        // - package, module or import declaration,
        // - "/**" in case of a pre-package (or pre-module) JavaDoc.
        val delimiter = "^(@|package|import|module|/\\*\\*)"

        licenseHeaderFile(licenseHeader, delimiter).updateYearWithLatest(updateLicenseYear)
    }

    kotlinGradle {
        target("*.gradle.kts", "boot-*/*.gradle.kts", "buildSrc/*.gradle.kts", "buildSrc/src/**/*.gradle.kts")
        targetExclude("**/build/**")

        ktlint("1.8.0").editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }

    format("yaml") {
        target("**/*.yml", "**/*.yaml")
        targetExclude("**/build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
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
}

defaultTasks("spotlessApply", "build")
