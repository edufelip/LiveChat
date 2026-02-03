import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
}

val ciVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull()
val ciVersionName = project.findProperty("versionName") as String?

android {
    namespace = "com.edufelip.livechat"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "com.edufelip.livechat"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = ciVersionCode ?: 1
        versionName = ciVersionName ?: "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(localPropsFile.inputStream())
        }
        val storageBucketUrl =
            (
                localProps.getProperty("storage.bucket.url")
                    ?: System.getenv("STORAGE_BUCKET_URL")
                    ?: ""
            )
        buildConfigField("String", "STORAGE_BUCKET_URL", "\"$storageBucketUrl\"")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "LiveChat Dev")
            buildConfigField("String", "FLAVOR_NAME", "\"dev\"")
            buildConfigField("boolean", "IS_DEV", "true")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "LiveChat")
            buildConfigField("String", "FLAVOR_NAME", "\"prod\"")
            buildConfigField("boolean", "IS_DEV", "false")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.android.material)
    implementation(libs.firebase.messaging)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.datetime)
    debugImplementation(libs.compose.ui.tooling)
}
