package com.project.livechat.domain.models

data class ConversationUiState(
    val conversationId: String = "",
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
