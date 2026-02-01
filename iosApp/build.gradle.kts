import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

plugins {
    kotlin("multiplatform")
}

kotlin {
    val appName = "LiveChat"
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<KotlinNativeTarget>().all {
        binaries {
            executable {
                baseName = appName
                entryPoint = "com.edufelip.livechat"
                linkerOpts(
                    "-framework",
                    "UIKit",
                    "-framework",
                    "Metal",
                    "-framework",
                    "CoreText",
                    "-framework",
                    "CoreGraphics",
                    "-lsqlite3",
                )
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":app"))
            }
        }
        val commonTest by getting
    }
}

private val iosSimulatorDevice: String
    get() = project.findProperty("IOS_SIMULATOR_DEVICE")?.toString() ?: "iPhone 15 Pro"

private val bundleId = "com.edufelip.livechat"
private val appName = "LiveChat"

private fun KotlinNativeTarget.debugExecutable() = binaries.getExecutable(NativeBuildType.DEBUG)

fun registerSimulatorTasks() {
    val execOperations = project.serviceOf<ExecOperations>()
    val simulatorTarget = kotlin.targets.getByName("iosSimulatorArm64") as KotlinNativeTarget
    val debugExecutable = simulatorTarget.debugExecutable()

    val bundleDirProvider = layout.buildDirectory.dir("ios/simulator/debug/$appName.app")

    val bundleTask =
        tasks.register("bundleIosSimulatorArm64Debug", Sync::class.java) {
            dependsOn(debugExecutable.linkTaskProvider)
            from("src/iosMain/resources/Info.plist")
            from(debugExecutable.outputFile) {
                rename { appName }
            }
            into(bundleDirProvider)
            outputs.dir(bundleDirProvider)
            doLast {
                val executable = bundleDirProvider.get().file(appName).asFile
                try {
                    val perms =
                        setOf(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ,
                            PosixFilePermission.GROUP_EXECUTE,
                            PosixFilePermission.OTHERS_READ,
                            PosixFilePermission.OTHERS_EXECUTE,
                        )
                    Files.setPosixFilePermissions(executable.toPath(), perms)
                } catch (t: Throwable) {
                    logger.warn("Unable to mark $executable as executable; simulator launch may fail.", t)
                }
            }
        }

    tasks.register("iosSimulatorArm64DebugRun") {
        group = "verification"
        description = "Builds and launches the LiveChat app in the iOS simulator."
        dependsOn(bundleTask)
        val deviceProvider = providers.provider { iosSimulatorDevice }
        val bundleDirProviderFinal = bundleDirProvider
        doLast {
            val deviceName = deviceProvider.get()
            val bundlePath = bundleDirProviderFinal.get().asFile.absolutePath
            execOperations.exec { commandLine("xcrun", "simctl", "bootstatus", deviceName, "-b") }
            execOperations.exec { commandLine("xcrun", "simctl", "install", deviceName, bundlePath) }
            execOperations.exec { commandLine("xcrun", "simctl", "launch", deviceName, bundleId) }
        }
    }
}

registerSimulatorTasks()
