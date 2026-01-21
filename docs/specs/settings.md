# Settings Specifications

## UI Components
- **SettingsScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/screens/SettingsScreen.kt`
- **AccountSettingsScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/account/AccountSettingsScreen.kt`

## State Management
Presenters:
- `AccountPresenter.kt`
- `PrivacySettingsPresenter.kt`
- `AppearanceSettingsPresenter.kt`

## Privacy Persistence
Stored in Firestore and locally cached in `PrivacySettingsStore.kt`.

## Re-authentication Flow
Implemented in `AccountPresenter.kt` using `deleteAccount()` use case which handles `RecentLoginRequiredException`.
