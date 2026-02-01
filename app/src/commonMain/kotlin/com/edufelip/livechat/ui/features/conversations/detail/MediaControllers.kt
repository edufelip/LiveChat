package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable

enum class PermissionStatus {
    GRANTED,
    DENIED,
    BLOCKED,
}

sealed class MediaResult<out T> {
    data class Success<T>(
        val value: T,
    ) : MediaResult<T>()

    data class Permission(
        val status: PermissionStatus,
    ) : MediaResult<Nothing>()

    object Cancelled : MediaResult<Nothing>()

    data class Error(
        val message: String? = null,
    ) : MediaResult<Nothing>()
}

interface ConversationMediaController {
    suspend fun pickImage(): MediaResult<String>

    suspend fun capturePhoto(): MediaResult<String>

    suspend fun startAudioRecording(): MediaResult<Unit>

    suspend fun stopAudioRecording(): String?
}

@Composable
expect fun rememberConversationMediaController(): ConversationMediaController
