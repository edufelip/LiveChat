package com.edufelip.livechat.domain.models

data class ConversationUiState(
    val conversationId: String = "",
    val contactName: String? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val participant: Participant? = null,
    val isMuted: Boolean = false,
    val muteUntil: Long? = null,
    val isArchived: Boolean = false,
)
