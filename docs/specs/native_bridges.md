# Native Bridges & Platform Integration

LiveChat uses a bridge pattern to integrate platform-specific features (Android and iOS) with the shared Kotlin Multiplatform (KMP) logic.

## The Bridge Pattern

The core logic in `shared/domain` and `shared/data` often needs to perform operations that are implemented differently on Android and iOS (e.g., Firebase SDK calls, file system access, hardware sensors).

1.  **Interface Definition**: Interfaces are defined in `shared/data/src/commonMain` (e.g., `AuthBridge`, `MessagesRemoteBridge`).
2.  **Android Implementation**: Located in `shared/data/src/androidMain`. These typically use the standard Firebase Android SDKs.
3.  **iOS Implementation**: 
    - The shared code expects an implementation of the interface.
    - The actual logic is often written in **Swift** within the `iosApp` directory.
    - These Swift classes (e.g., `FirebaseMessagesBridge.swift`) are passed into the KMP shared module during startup via `LiveChatBridgeFactory.swift` and `IosBridgeBundle.kt`.

## Core Bridges

### Auth Bridge
- **Purpose**: Handles phone authentication, OTP verification, and session management.
- **Android**: `PhoneAuthBridge.kt` (Android SDK).
- **iOS**: `FirebasePhoneAuthBridge.swift` (iOS SDK).

### Messages & Storage Bridge
- **Purpose**: Real-time message listeners and media (image/audio) uploads.
- **Android**: Uses Firestore and Firebase Storage Android SDKs.
- **iOS**: Uses Firestore and Firebase Storage iOS SDKs via Swift bridges.

### Contacts Bridge
- **Purpose**: Accesses the native device contacts/address book.
- **Android**: Uses `ContentResolver`.
- **iOS**: Uses `Contacts` framework.

## Platform-Specific UI Components

While most UI is shared via Compose Multiplatform, some elements remain native or require platform-specific tuning:
- **Audio Recording**: High-level control is in Compose, but the underlying `AudioRecorder` is platform-specific.
- **Image Picker**: Uses the native system gallery picker on both platforms.
- **Status Bar & Insets**: Managed differently to respect Android's `WindowInsets` and iOS's `Safe Area`.
