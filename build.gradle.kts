buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.google.services)
        classpath(libs.kotlin.gradle)
    }
}

plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register("testSharedDataUnitTest") {
    group = "verification"
    description = "Runs unit tests for :shared:data"
    dependsOn(":shared:data:testDebugUnitTest")
}

tasks.register("testSharedDomainUnitTest") {
    group = "verification"
    description = "Runs unit tests for :shared:domain"
    dependsOn(":shared:domain:testDebugUnitTest")
}

tasks.register("testComposeAppUnitTest") {
    group = "verification"
    description = "Runs unit tests for :composeApp"
    dependsOn(":composeApp:testDebugUnitTest")
}

tasks.register("testAllModuleUnitTests") {
    group = "verification"
    description = "Runs unit tests for shared data, shared domain, and compose app modules"
    dependsOn(
        "testSharedDataUnitTest",
        "testSharedDomainUnitTest",
        "testComposeAppUnitTest",
    )
}
