# Home Screen Specifications

## UI Components
- **LiveChatApp**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt` (Container)
- **HomeScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/home/view/HomeScreen.kt`
- **HomeTabs**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/navigation/HomeTabs.kt`

## State Management
- **Presenter**: `AppPresenter.kt`
- **Model**: `HomeUiState` (stores active tab, unread counts, etc.)

## Navigation
The app uses a custom `AnimatedContent` transition within `LiveChatApp.kt` to handle high-level destination changes (Welcome vs. Onboarding vs. Home).
