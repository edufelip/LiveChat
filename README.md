# LiveChat
[![Author](https://img.shields.io/static/v1?label=@author&message=Eduardo%20Santos&color=navy)](https://github.com/edufelip)
[![LinkedIn](https://img.shields.io/static/v1?label=@linkedin&message=@edu_santos&color=blue)](https://www.linkedin.com/in/eduardo-felipe-dev/)

LiveChat is a Kotlin Multiplatform chat experience geared toward organizing conversations into categories. The project currently ships an Android client built with Jetpack Compose and shares its data/domain stack with iOS via `:shared` modules.

## Current Stack
- **Presentation (Android)**: Jetpack Compose, Navigation Compose, Material 3.
- **Dependency Injection**: [Koin](https://insert-koin.io/) for both Android and shared modules (Hilt removed).
- **State/Data**: Kotlin Coroutines/Flows, shared ViewModels created in `app/src/main/java/com/project/livechat/ui/viewmodels`, Koin adapters in `androidViewModelModule`.
- **Persistence**: [SQDelight](https://cashapp.github.io/sqldelight/) schema defined under `shared/data/src/commonMain/sqldelight` with drivers provided per platform.
- **Remote**:
  - Android uses Firebase Auth for phone verification and the shared REST client (`FirebaseRestContactsRemoteData`) to query Firestore via Ktor/OkHttp. Configuration is pulled from `google-services.json` at runtime.
  - iOS shares the same REST client using the Darwin engine; `initKoinForIos()` now loads `FirebaseRestConfig` from `GoogleService-Info.plist` (or you can pass an explicit `FirebaseRestConfig` when credentials change).
- **Testing**: Shared MPP tests cover the SQDelight data source and Koin bootstrap (`shared/data/src/commonTest`). Android instrumented tests were removed during the migration; add new ones as needed.

## Getting Started
Clone this repository and open the root project in **Android Studio** (Kotlin Multiplatform support enabled).
```bash
https://github.com/edufelip/live-chat_android.git
```

### Android Build Commands
```bash
./gradlew :app:assembleDebug
./gradlew :app:lint
./gradlew :app:testDebugUnitTest
```
> Building requires a local `app/google-services.json` (not tracked). Place your Firebase configuration there before running Gradle.

### Shared Modules
- `:shared:data` — SQDelight database, Koin modules, Firebase REST client (Ktor), multiplatform repositories.
- `:shared:domain` — Models, use cases, validation utilities, shared Koin module.

### iOS Bootstrap
When wiring the SwiftUI entry point, either call the new helper that reads credentials from `GoogleService-Info.plist`:

```swift
import Shared

initKoinForIos() // loads FirebaseRestConfig using the bundled plist
```

or supply explicit values:

```swift
initKoinForIos(
    config: FirebaseRestConfig(
        projectId: "your-project-id",
        apiKey: "your-firebase-web-api-key",
        usersCollection: "users"
    )
)
```

### Dependency Graph
Koin bootstraps in `MyApplication` via `initSharedKoin`, wiring:
- `androidPlatformModule` (Firebase SDK bindings, connectivity observer, SQL driver).
- `androidViewModelModule` (Compose ViewModels).
- Shared modules from `:shared:data` and `:shared:domain`.

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
