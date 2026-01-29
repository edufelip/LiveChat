# Home Screen Specifications

## UI Components
- **LiveChatApp**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt` (Container)
- **HomeLayerHost**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/HomeLayerHost.kt` (Home flow container)
- **HomeScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/home/view/HomeScreen.kt`
- **HomeTabs**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/navigation/HomeTabs.kt`

## State Management
- **Presenter**: `AppPresenter.kt`
- **Model**: `HomeUiState` (stores active tab, unread counts, etc.)

## Navigation
The app uses a custom `AnimatedContent` transition within `LiveChatApp.kt` to handle high-level destination changes (Welcome vs. Onboarding vs. Home).
Within Home, `HomeLayerHost.kt` owns full-screen navigation for contacts, conversation details, and settings subpages so those destinations do not show the bottom bar.
