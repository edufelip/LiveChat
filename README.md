# LiveChat
[![Author](https://img.shields.io/static/v1?label=@author&message=Eduardo%20Santos&color=navy)](https://github.com/edufelip)
[![LinkedIn](https://img.shields.io/static/v1?label=@linkedin&message=@edu_santos&color=blue)](https://www.linkedin.com/in/eduardo-felipe-dev/)

LiveChat is a Kotlin Multiplatform chat experience geared toward organizing conversations into categories. The project ships an Android client built with Jetpack Compose and a Compose Multiplatform iOS client that share the same domain/data stack.

## Current Stack
- **Presentation (Android)**: Compose Multiplatform hosted directly from `:composeApp` (`MainActivity`, `LiveChatApp`). The legacy `:app` module has been excluded from the build (see `app/README_LEGACY.md`) and now serves only as historical reference while remaining utilities are ported.
- **Presentation (iOS)**: Compose Multiplatform running from the `composeApp` module. The UI consumes shared presenters (`ConversationListPresenter`, `ConversationPresenter`, `ContactsPresenter`) directly via Koin through expect/actual hooks.
- **Dependency Injection**: [Koin](https://insert-koin.io/) for both Android and shared modules (Hilt removed).
- **State/Data**: Kotlin Coroutines/Flows. Legacy Android ViewModels still live under `app/src/main/java/com/project/livechat/ui/viewmodels`, but the `:composeApp` entry now consumes shared presenters directly.
- **Persistence**: [SQDelight](https://cashapp.github.io/sqldelight/) schema defined under `shared/data/src/commonMain/sqldelight` with drivers provided per platform.
- **Remote**:
  - Android uses Firebase Auth for phone verification and resolves Firestore clients from the shared `firebaseBackendModule`. Configuration is pulled from `google-services.json` at runtime.
  - iOS reuses the same backend module with a Darwin `HttpClient`; supply credentials via `GoogleService-Info.plist` or pass a manual `FirebaseRestConfig`.
- **Backend Abstraction**: Koin boots with a configurable backend module list. The default `firebaseBackendModule` wires Firestore/Realtime listeners today, but you can replace it with your own implementation that provides `IContactsRemoteData`, `IMessagesRemoteData`, and `UserSessionProvider` bindings for a dedicated service.
- **Testing**: Shared MPP tests cover the SQDelight data source and Koin bootstrap (`shared/data/src/commonTest`). Android instrumented tests were removed during the migration; add new ones as needed.

## Getting Started
Clone this repository and open the root project in **Android Studio** (Kotlin Multiplatform support enabled).
```bash
https://github.com/edufelip/live-chat_android.git
```

### Android Build Commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:lint
./gradlew :composeApp:testDebugUnitTest
```
> Building still relies on a Firebase configuration file. Place your `google-services.json` under `composeApp/src/google-services.json` (or the appropriate variant directory such as `composeApp/src/debug/google-services.json`).
>
> Gradle runs on the bundled JetBrains Runtime 17 (`org.gradle.java.home` in `gradle.properties`). Keep that runtime installed locally or update the path if you swap JDKs.

### Shared Modules
- `:shared:data` — SQDelight database, Koin modules, Firebase REST client (Ktor), multiplatform repositories.
- `:shared:domain` — Models, use cases, validation utilities, shared Koin module.
- `:composeApp` — Compose Multiplatform UI, organised into `ui/app`, `ui/features`, `ui/components`, `ui/state`, and `ui/util` packages following an atomic design breakdown.

### Compose Multiplatform iOS Client
The iOS experience is now entirely Compose Multiplatform. There are two ways to work with it:

- **Run directly from Gradle** &mdash; `iosApp/` wraps the shared UI in a Kotlin/Native executable and handles simulator deployment:

  ```bash
  ./gradlew :iosApp:iosSimulatorArm64DebugRun
  ```

  Use `-PIOS_SIMULATOR_DEVICE="iPhone 14"` (or any simulator name returned by `xcrun simctl list devices`) to target a different simulator. The task builds `iosApp/build/ios/simulator/debug/LiveChat.app`, installs it, and launches it in the chosen device.

- **Consume from Xcode** &mdash; `composeApp/` produces an XCFramework you can drop into an Xcode project or Swift package:

  ```bash
  ./gradlew :composeApp:assembleLiveChatComposeXCFramework
  ```

  You’ll find the framework under `composeApp/build/XCFrameworks/LiveChatCompose`. Drag that folder into Xcode (or reference it through SwiftPM) and call `MainViewController()`/`updateLiveChatSession()` from Swift just like before&mdash;no SwiftUI glue code required.

- **Use the bundled Xcode target** &mdash; `iosApp/LiveChatIOS.xcodeproj` already links the generated `LiveChatCompose.xcframework` and boots the UIKit wrapper (`AppDelegate` + `MainViewControllerKt`).
  1. Generate the framework first (command above).
  2. Open the project in Xcode, set your Development Team under *Signing & Capabilities*, and build/run against a simulator or device.
  3. The target ships with sensible defaults:
     - Deployment target `iOS 17.2` to match the Compose runtime.
     - `libsqlite3` linked and SDK `System/Library/SubFrameworks` added to resolve Compose’s dependencies such as `UIUtilities`.
     - `CADisableMinimumFrameDurationOnPhone` enabled in `iosApp/LiveChatIOS/Info.plist` to satisfy Compose’s high-refresh-rate sanity check.
     - `AppDelegate` calls `FirebaseApp.configure()` automatically when the FirebaseCore SDK is present (wrapped in `#if canImport(FirebaseCore)`).

Configuration pointers:

- Configure Firebase by editing `defaultFirebaseConfig()` in `composeApp/src/iosMain/kotlin/com/project/livechat/composeapp/MainViewController.kt` or by supplying a custom `FirebaseRestConfig` when wiring `MainViewController`.
- Update the active session at runtime from Kotlin with:

  ```kotlin
  updateLiveChatSession(userId = "real-user-id", idToken = "optional-token")
  ```

- The previous SwiftUI shell has been removed in favor of this KMP-first packaging flow. Historical sources remain under `docs/legacy-ios-swift/` if needed.

- Drop your platform credentials into `iosApp/LiveChatIOS/GoogleService-Info.plist`. The path is git-ignored by default; copy your Firebase file locally if you need Analytics/Auth in the Xcode build.

You can still generate the shared SQLDelight XCFramework for other native integrations:

  ```bash
  ./gradlew :shared:data:assembleLiveChatSharedReleaseXCFramework
  ```

### Dependency Graph
Koin now bootstraps in `LiveChatApplication` via `startKoinForAndroid`, wiring:
- `androidPlatformModule` (Firebase App/Auth, SQLDelight driver, HTTP client, session provider) plus any extra backend modules you pass in.
- `firebaseBackendModule` (Firestore contacts/messages clients, session provider bindings).
- Shared modules from `:shared:data` and `:shared:domain`.

On iOS, `startKoinForiOS` registers the same shared modules alongside `iosPlatformModule` (Darwin HTTP client, native SQLDelight driver, in-memory session provider) plus the backend module list you provide—defaulting to `firebaseBackendModule`.

### UI Architecture at a Glance
- **Atomic Compose components**: Reusable building blocks live in `composeApp/src/commonMain/kotlin/com/project/livechat/composeapp/ui/components` and `app/src/main/java/com/project/livechat/ui/components`, grouped into `atoms`, `molecules`, `organisms`, and `dialogs`. Each component includes a local preview to simplify iteration.
- **Feature-oriented packages**: Screens and screen-specific state/route composables sit inside `ui/features/<feature-name>/**` on both the shared and Android modules (e.g., `ui/features/conversations/list`, `ui/features/contacts/screens`).
- **Presenter bridge**: Shared presenters are exposed through expect/actual helpers in `composeApp/ui/state/PresenterHooks.kt`, now backed by Koin on both iOS and Android via `IosKoinBridge` and `AndroidKoinBridge`.
- **Firebase setup**: The Google Services Gradle plugin runs inside `:composeApp`. Drop `google-services.json` into `composeApp/src/<buildType>/` (or directly under `composeApp/src/`) before running Gradle so the plugin can merge Firebase credentials.

## Layout Preview
<br>
  <p align="left">
    <img alt="splash screen"
         src="https://github.com/edufelip/live-chat_android/assets/34727187/b19ec81a-42d6-4a19-8150-89c30e4f8ec5"
         width="20%"
         title="main screen">

## Contributing
1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push your branch (`git push origin my-new-feature`)
5. Open a Pull Request and describe the changes

## Maintainer
- [Eduardo Felipe](http://github.com/edufelip)
