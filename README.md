# LiveChat
[![Author](https://img.shields.io/static/v1?label=@author&message=Eduardo%20Santos&color=navy)](https://github.com/edufelip)
[![LinkedIn](https://img.shields.io/static/v1?label=@linkedin&message=@edu_santos&color=blue)](https://www.linkedin.com/in/eduardo-felipe-dev/)

LiveChat is a Kotlin Multiplatform chat experience geared toward organizing conversations into categories. The project ships an Android client built with Jetpack Compose and a Compose Multiplatform iOS client that share the same domain/data stack.

## Current Stack
- **Presentation (Android)**: Compose Multiplatform hosted directly from `:composeApp` (`MainActivity`, `LiveChatApp`). The legacy `:app` module has been excluded from the build (see `app/README_LEGACY.md`) and now serves only as historical reference while remaining utilities are ported.
- **Presentation (iOS)**: Compose Multiplatform running from the `composeApp` module. The UI consumes shared presenters (`ConversationListPresenter`, `ConversationPresenter`, `ContactsPresenter`) directly via Koin through expect/actual hooks.
- **Dependency Injection**: [Koin](https://insert-koin.io/) for both Android and shared modules (Hilt removed).
- **State/Data**: Kotlin Coroutines/Flows. Legacy Android ViewModels still live under `app/src/main/java/com/project/livechat/ui/viewmodels`, but the `:composeApp` entry now consumes shared presenters directly.
- **Persistence**: [Room](https://developer.android.com/training/data-storage/room) configured for Kotlin Multiplatform with bundled SQLite drivers and shared entities/DAOs under `shared/data/src/commonMain`.
- **Remote**:
  - Android uses Firebase Auth for phone verification and resolves Firestore clients from the shared `firebaseBackendModule`. Configuration is pulled from `google-services.json` at runtime.
  - iOS reuses the same backend module with a Darwin `HttpClient`; supply credentials via `GoogleService-Info.plist` or pass a manual `FirebaseRestConfig`.
- **Backend Abstraction**: Koin boots with a configurable backend module list. The default `firebaseBackendModule` wires Firestore/Realtime listeners today, but you can replace it with your own implementation that provides `IContactsRemoteData`, `IMessagesRemoteData`, and `UserSessionProvider` bindings for a dedicated service.
- **Testing**: Shared MPP tests cover the SQDelight data source and Koin bootstrap (`shared/data/src/commonTest`). Android instrumented tests were removed during the migration; add new ones as needed. Onboarding persistence now ships dedicated domain + Android unit tests to keep the DataStore snapshot path verified.

### Test Batches (Onboarding Persistence)
Run these tasks anytime onboarding storage or presenters change:

```bash
./gradlew :shared:domain:test
./gradlew :shared:data:test
```
Together they cover the AppPresenter snapshot logic plus the shared Room persistence layer across Android/iOS targets.

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

### Theming
- The entire UI now runs through a `LiveChatTheme` wrapper (Compose Multiplatform) built on a pastel green palette. Shapes and typography are centralised under `ui/theme`.
- Platform colour selection is abstracted via a `PlatformColorSchemeStrategy`. Android’s actual strategy opts into Material You when available; iOS and other targets fall back to the shared pastel palette.
- Preview helpers reuse the same theme, guaranteeing screenshots and design reviews match runtime output.
- Components reference `MaterialTheme` tokens instead of hard-coded colours. Empty states, badges, and the contacts screen now draw from `colorScheme` for consistent styling.

### Contacts Sync & Permissions
- A cross-platform `ContactsPermissionManager` abstraction lives in `composeApp/contacts`. The Android actual requests `READ_CONTACTS` at runtime via `ActivityResultContracts`, while the iOS actual simply returns granted—matching the system behaviour.
- `ContactsRoute` requests permission on entry and kicks off a sync when granted. The sync path calls into shared presenters/use cases, keeping the logic consistent across platforms.
- `CheckRegisteredContactsUseCase` now diff-checks the on-device list against the cached SQDelight table:
  - Removes contacts that disappeared from the phone.
  - Inserts new phone contacts into the local store.
  - Updates changed display info.
  - Hits Firebase (through `IContactsRemoteData`) to determine which contacts are registered and persists the flag in SQL.
- The `contacts` table gained an `is_registered` column (`shared/data/.../contacts.sq` + migration `002_add_is_registered_column.sqm`) so the UI can render verified users immediately on later visits.
- The shared `ContactsPresenter` exposes `ContactsEvent` emissions for navigation and invites. The Compose UI marks LiveChat users as tappable rows (opening conversations) and shows an `Invite` button for others.
- Tests cover the diff logic (`CheckRegisteredContactsUseCaseTest`) and presenter state/event flow (`ContactsPresenterTest`) in `shared/domain/src/commonTest`.

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

  You’ll find the framework under `composeApp/build/XCFrameworks/LiveChatCompose`. Drag that folder into Xcode (or reference it through SwiftPM) and call `MainViewController(...)`/`updateLiveChatSession()` from Swift just like before&mdash;no SwiftUI glue code required. `MainViewController` now requires an iOS bridge bundle so Kotlin can route Firebase calls to Swift.

- **Use the bundled Xcode target** &mdash; `iosApp/LiveChatIOS.xcodeproj` already links the generated `LiveChatCompose.xcframework` and boots the UIKit wrapper (`AppDelegate` + `MainViewControllerKt`).
  1. Generate the framework first (command above).
  2. Open the project in Xcode, set your Development Team under *Signing & Capabilities*, and build/run against a simulator or device.
  3. The target ships with sensible defaults:
     - Deployment target `iOS 17.2` to match the Compose runtime.
     - `libsqlite3` linked and SDK `System/Library/SubFrameworks` added to resolve Compose’s dependencies such as `UIUtilities`.
     - `CADisableMinimumFrameDurationOnPhone` enabled in `iosApp/LiveChatIOS/Info.plist` to satisfy Compose’s high-refresh-rate sanity check.
     - `AppDelegate` calls `FirebaseApp.configure()` automatically when the FirebaseCore SDK is present (wrapped in `#if canImport(FirebaseCore)`).

Configuration pointers:

- Configure Firebase by editing `defaultFirebaseConfig()` in `composeApp/src/iosMain/kotlin/com/edufelip/livechat/MainViewController.kt` or by supplying a custom `FirebaseRestConfig` when wiring `MainViewController`.
- Update the active session at runtime from Kotlin with:

  ```kotlin
  updateLiveChatSession(userId = "real-user-id", idToken = "optional-token")
  ```

- The previous SwiftUI shell has been removed in favor of this KMP-first packaging flow. Historical sources remain under `docs/legacy-ios-swift/` if needed.

- Drop your platform credentials into `iosApp/LiveChatIOS/GoogleService-Info.plist`. The path is git-ignored by default; copy your Firebase file locally if you need Analytics/Auth in the Xcode build.

You can still generate the shared Room-powered XCFramework for other native integrations:

  ```bash
  ./gradlew :shared:data:assembleLiveChatSharedReleaseXCFramework
  ```

### Dependency Graph
Koin now bootstraps in `LiveChatApplication` via `startKoinForAndroid`, wiring:
- `androidPlatformModule` (Firebase App/Auth, Room DB builder with bundled SQLite, HTTP client, session provider) plus any extra backend modules you pass in.
- `firebaseBackendModule` (Firestore contacts/messages clients, session provider bindings).
- Shared modules from `:shared:data` and `:shared:domain`.

On iOS, `startKoinForiOS` registers the same shared modules alongside `iosPlatformModule` (Darwin HTTP client, Room builder backed by the bundled driver, in-memory session provider) plus the backend module list you provide—defaulting to `firebaseBackendModule`.

### Messaging Model Snapshot
- The shared `Message` entity now tracks delivery sequencing (`messageSeq`), ack timestamps (`serverAckAt`), rich content typing (`MessageContentType`), encrypted payloads (`ciphertext`), attachments, reply/thread pointers, and lifecycle metadata (`editedAt`, `deletedForAllAt`, `metadata`). UI presenters keep using the same APIs while getting the extra context for roadmap features like threads or E2EE.
- The Room entities mirror those fields, so the bundled SQLite database persists ciphertext, sequences, and moderation markers without losing compatibility with existing queries. `MessagesLocalDataSource` and its mapper were updated to read/write the new columns.
- Spec-aligned primitives for conversations, participants, receipts, and reactions now live under `shared/domain/.../ConversationModels.kt` and `MessageArtifacts.kt` to support upcoming roadmap work (pinned messages, delivery receipts, etc.).
- Remote adapters now exchange the richer envelope with Firestore (content type, ciphertext, attachments, reply/thread metadata). SQLite already stores the same schema, so new delivery receipts or E2EE payloads travel end-to-end without extra migrations.
- A dedicated `ConversationParticipantsRepository` exposes participant state (mute, archived, pinned, read markers) via shared flows so presenters and future delivery-receipt logic can rely on a single source of truth.
- Conversation lists hide archived chats by default, add an `Archived` filter chip, and surface mute/archive toggles on each row so participant state stays manageable without opening the detail view.
- Contacts invites now pass the target contact through `InviteShareRequest`, so every tap opens the native Android/iOS share sheet (no invite history throttling) with a pre-filled message ready to send via SMS, email, or WhatsApp.
- Conversation presenters now auto-dispatch read receipts: when `observeConversation` emits newer messages, we contrast them with the participant’s `lastReadSeq/lastReadAt` and call `MarkConversationReadUseCase`. This keeps delivery receipts, mute/archived UI, and unread badges in sync across detail and list presenters.
- Because we only ship fresh installs today, Room simply uses destructive migrations (schema export lives under `shared/data/schemas`). Update the entity definitions whenever you tweak tables and let the bundled driver recreate the DB on install.

### Firestore Security Rules Cheatsheet
Keep the storage schema from `plan.md` locked down with the following baseline. The helper functions use brace bodies instead of assignment syntax, avoiding the `Unexpected '='` error you hit at lines 77/83:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isSignedIn() {
      return request.auth != null;
    }

    function isParticipant(conversationId) {
      return isSignedIn() &&
        exists(/databases/$(database)/documents/conversations/$(conversationId)/participants/$(request.auth.uid));
    }

    match /users/{userId} {
      allow read, write: if isSignedIn() && request.auth.uid == userId;
    }

    match /conversations/{conversationId} {
      allow read: if isParticipant(conversationId);
      allow write: if false; // conversation docs are created server-side

      match /participants/{userId} {
        allow read: if isParticipant(conversationId);
        allow write: if request.auth.uid == userId;
      }

      match /messages/{messageId} {
        allow read: if isParticipant(conversationId);
        allow create: if isParticipant(conversationId)
          && request.resource.data.senderId == request.auth.uid;
        allow update, delete: if false;
      }

      match /receipts/{receiptId} {
        allow read: if isParticipant(conversationId);
        allow write: if isParticipant(conversationId)
          && request.resource.data.userId == request.auth.uid;
      }

      match /reactions/{reactionId} {
        allow read, write: if isParticipant(conversationId)
          && request.resource.data.userId == request.auth.uid;
      }
    }
  }
}
```

### UI Architecture at a Glance
- **Atomic Compose components**: Reusable building blocks live in `composeApp/src/commonMain/kotlin/com/edufelip/livechat/ui/components` and `app/src/main/java/com/project/livechat/ui/components`, grouped into `atoms`, `molecules`, `organisms`, and `dialogs`. Each component includes a local preview to simplify iteration.
- **Feature-oriented packages**: Screens and screen-specific state/route composables sit inside `ui/features/<feature-name>/**` on both the shared and Android modules (e.g., `ui/features/conversations/list`, `ui/features/contacts/screens`).
- **Presenter bridge**: Shared presenters are exposed through expect/actual helpers in `composeApp/ui/state/PresenterHooks.kt`, now backed by Koin on both iOS and Android via `IosKoinBridge` and `AndroidKoinBridge`.
- **Animated navigation**: Tab switches (Chats ↔ Contacts) run through `AnimatedContent`, providing directional fades/slides that align with the new theme.
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
