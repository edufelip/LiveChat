# LiveChat iOS

This folder contains a SwiftUI client that consumes the shared Kotlin Multiplatform data and domain layers.

## Project layout

- `LiveChatIOSApp.swift` – SwiftUI entry point bootstrapping Koin and wiring the root view
- `Conversations/` – Conversation screen, view model, and message bubble components
- `Support/` – Helpers for starting Koin, adapting Kotlin collections, and other shared utilities

## Getting started

1. Generate the shared Kotlin framework:

   ```bash
   ./gradlew :shared:data:assembleLiveChatSharedReleaseXCFramework
   ```

   This produces `shared/data/build/XCFrameworks/release/LiveChatShared.xcframework` which can be added to the Xcode project.

2. Create an Xcode project (or workspace) and add the contents of `iosApp/`.

3. Add `LiveChatShared.xcframework` to the Xcode project and make sure it is linked with the application target.

4. Update the placeholder values in `LiveChatIOSApp.swift` with the real Firebase REST configuration and default conversation identifier.

5. Run on an iOS simulator or device.

## Session management

`KoinHolder.updateSession(userId:idToken:)` updates the shared `InMemoryUserSessionProvider`. Call this as soon as the user logs in so that outgoing messages have a sender.

## Conversation lifecycle

`ConversationViewModel` wraps the shared `ConversationPresenter`. It subscribes to the presenter flow, maps messages into Swift-friendly view models, and handles send/refresh requests from the SwiftUI layer.
