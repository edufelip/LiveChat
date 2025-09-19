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
}
