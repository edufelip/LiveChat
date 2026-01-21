# Chat Features Specifications

## Media Handling
- **Image Compressor**: `ImageCompressor.kt` (resizes and adjusts quality).
- **Audio Controller**: `AudioPlayerController.kt` (manages `MediaPlayer` / `AVAudioPlayer`).
- **File Store**: `MediaFileStore.kt` (local cache management).

## UI Components
- **ComposerBar**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/detail/components/ComposerBar.kt`
- **AudioBubble**: Custom view inside `MessageBubble.kt`.

## Storage Path Convention
`messages/{receiverId}/{senderId}/{timestamp}.{ext}`
(Managed via `FirebaseStorageBridge`).
