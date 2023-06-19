apply("${project.rootDir}/buildscripts/toml-updater-config.gradle")

plugins {
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.hilt) apply (false)
}

buildscript {
    dependencies {
        classpath(libs.firebase.google.services)
    }
}
