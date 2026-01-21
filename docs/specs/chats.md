# Chats Specifications

## UI Components
- **ConversationListScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/list/screens/ConversationListScreen.kt`
- **ConversationDetailScreen**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/detail/screens/ConversationDetailScreen.kt`
- **MessageBubble**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/detail/components/MessageBubble.kt`

## Data Models
- **MessageEntity**: Local database representation.
- **MessageStatus**: Enum (SENDING, SENT, DELIVERED, READ, ERROR).

## Presenters
- `ConversationListPresenter.kt`
- `ConversationPresenter.kt`

## Presence Logic
Presence is polled and updated via `PresenceRepository.kt`. 
Status is considered "Offline" after 2 minutes of inactivity (`ACTIVE_WINDOW_MS = 120,000`).
