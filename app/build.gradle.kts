import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.gradle.api.tasks.Sync

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hot.reload)
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    cocoapods {
        summary = "LiveChat Compose UI"
        homepage = "https://example.com/livechat"
        ios.deploymentTarget = "17.2"
        pod("PhoneNumberKit", version = "~> 4.2")
        framework {
            isStatic = true
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    androidLibrary {
        namespace = "com.edufelip.livechat.shared"
        compileSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        buildToolsVersion = "35.0.1"
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    targets.withType<KotlinAndroidTarget>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    val liveChatXcFramework = XCFramework("LiveChatCompose")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material)
                implementation(libs.compose.animation)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.datetime)
                api(project(":shared:data"))
                implementation(libs.compose.ui.tooling.preview)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.messaging)
                implementation(libs.navigationevent.compose)
                implementation(libs.libphonenumber)
            }
        }
        val androidHostTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.uiautomator)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }

    jvmToolchain(17)

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "LiveChatCompose"
            isStatic = true
            export(project(":shared:data"))
            liveChatXcFramework.add(this)
        }
    }
}



compose {
    resources {
        packageOfResClass = "com.edufelip.livechat.resources"
    }
}

val preparedComposeResources =
    layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources")
val androidDeviceTestAssetsOut =
    layout.buildDirectory.dir("intermediates/assets/androidDeviceTest/mergeAndroidDeviceTestAssets")
val androidDeviceTestAssetsDir = layout.projectDirectory.dir("src/androidDeviceTest/assets")

tasks.matching { it.name == "mergeAndroidDeviceTestAssets" }.configureEach {
    dependsOn("prepareComposeResourcesTaskForCommonMain")
    inputs.dir(androidDeviceTestAssetsDir)
    doLast {
        copy {
            from(preparedComposeResources) {
                into("composeResources/com.edufelip.livechat.resources")
            }
            into(androidDeviceTestAssetsOut)
        }
        copy {
            from(androidDeviceTestAssetsDir)
            into(androidDeviceTestAssetsOut)
        }
    }
}
