# LiveChat iOS packaging module

This Gradle module produces the iOS application bundle around the Compose Multiplatform UI exposed by `:app`.

## Build & run on the simulator

```
./gradlew :iosApp:iosSimulatorArm64DebugRun
```

The task will:
1. Compile the Kotlin/Native executable for the `iosSimulatorArm64` target.
2. Bundle the result together with `Info.plist` into `build/ios/simulator/debug/LiveChat.app`.
3. Install and launch the `.app` in the configured simulator (defaults to `iPhone 15 Pro`).

To target a different simulator, pass a `IOS_SIMULATOR_DEVICE` property:

```
./gradlew :iosApp:iosSimulatorArm64DebugRun -PIOS_SIMULATOR_DEVICE="iPhone 14"
```

## Artifacts

- Bundled `.app`: `build/ios/simulator/debug/LiveChat.app`
- Raw executable: `build/bin/iosSimulatorArm64/debugExecutable/LiveChat.kexe`
- XCFramework for Xcode integration: run `./gradlew :app:assembleLiveChatComposeXCFramework` and consume the output from `../app/build/XCFrameworks`

## Xcode target

An Xcode project is now available under `iosApp/iosApp.xcodeproj`. It embeds the `LiveChatCompose.xcframework` exposed by the shared Compose module and launches the Kotlin UI via `MainViewController(...)` with a Swift-side bridge bundle (so Firebase stays in the platform layer). Before opening the project, generate the framework:

```
./gradlew :app:assembleLiveChatComposeXCFramework
```

Then open `iosApp/iosApp.xcodeproj` in Xcode. The project expects code signing to be configured locally (set a development team under the “Signing & Capabilities” tab) and will look for the XCFramework under `app/build/XCFrameworks`. Re-run the Gradle task whenever you make changes to the shared code to refresh the framework that Xcode links against.

The Xcode target currently uses an iOS 17.2 deployment target to stay compatible with the Compose Multiplatform runtime. If Apple updates the SDK version baked into the generated frameworks, raise the deployment target accordingly. The project links `libsqlite3` and searches the SDK’s `System/Library/SubFrameworks` directory so that Compose’s transitively required system frameworks (e.g. `UIUtilities`) resolve at link time. `Info.plist` already enables `CADisableMinimumFrameDurationOnPhone` so Compose’s frame-rate sanity check passes. At launch the app will call `FirebaseApp.configure()` whenever the FirebaseCore SDK is available (the import is wrapped in `#if canImport(FirebaseCore)` so the project still builds without the framework). No asset catalog is bundled by default; drop one into `iosApp/iosApp/` and add it to the target if you need custom icons or launch images.

### Firebase setup

- Copy your environment-specific `GoogleService-Info.plist` into `iosApp/iosApp/`. The file is git-ignored by default so secrets stay out of source control.
- If you omit the file, the app still launches (Compose bootstraps with dummy config), but Firebase services will be unavailable.
- Additional Firebase SDKs can be linked via Swift Package Manager if needed; re-run `FirebaseApp.configure()` only happens when `FirebaseCore` is present.

### iOS E2E UI tests (Firebase)

The E2E UI test uses real Firebase Auth by passing `E2E_MODE=1` to the app. Configure test phone
numbers and verification codes in the Firebase Console, then set the following environment
variables when running the UI test:

- `E2E_PHONE` (e.g. 6505553434)
- `E2E_OTP` (e.g. 123123)

The test also supports `UITEST_RESET_ONBOARDING=1` to force onboarding on each run.
