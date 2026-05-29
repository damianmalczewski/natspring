import com.diffplug.spotless.LineEnding
import internal.getBooleanProperty

plugins {
    id("com.diffplug.spotless")
}

spotless {
    val licenseHeader = "${rootProject.rootDir}/gradle/license-header.java"
    val updateLicenseYear = getBooleanProperty("spotless.license-year-enabled")

    java {
        target("src/**/*.java")
        targetExclude("build/**")
        licenseHeaderFile(licenseHeader).updateYearWithLatest(updateLicenseYear)

        // NOTE: decided not to upgrade Google Java Format, as versions 1.29+ require running it on Java 21
        googleJavaFormat("1.28.0")
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    kotlin {
        target("src/**/*.kt")
        targetExclude("build/**")
        licenseHeaderFile(licenseHeader).updateYearWithLatest(updateLicenseYear)

        ktlint("1.8.0")
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("javaMisc") {
        target("src/**/package-info.java", "src/**/module-info.java")
        targetExclude("build/**")

        // The regex finds where code starts in these files so the license header can be placed before it.
        // Code starts with: any annotation, package/module/import declaration, or "/**" for pre-package JavaDoc.
        val delimiter = "^(@|package|import|module|/\\*\\*)"

        licenseHeaderFile(licenseHeader, delimiter).updateYearWithLatest(updateLicenseYear)
    }
    format("yaml") {
        target("src/**/*.yml", "src/**/*.yaml")
        targetExclude("build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}
