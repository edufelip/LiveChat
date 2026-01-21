# Contacts Specifications

## UI Components
- **ContactsScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/contacts/screens/ContactsScreen.kt`

## Domain Logic
- **Presenter**: `ContactsPresenter.kt`
- **Use Cases**:
    - `GetLocalContactsUseCase`: Platform-specific contact fetching.
    - `CheckRegisteredContactsUseCase`: Integration with Cloud Functions.

## Platform Implementation
- **Android**: `FirebaseContactsBridge.kt` (uses `ContentResolver`).
- **iOS**: `FirebaseContactsBridge.swift` (uses `CNContactStore`).

## Sync Session Management
Managed via `ContactsSyncSession.kt` (handles cooldowns and sync state).
