package com.project.livechat.domain.models

data class ConversationSummary(
    val conversationId: String,
    val contactName: String?,
    val contactPhoto: String?,
    val lastMessage: Message,
    val unreadCount: Int,
    val isPinned: Boolean,
    val pinnedAt: Long?,
    val lastReadAt: Long?
) {
    val displayName: String
        get() = contactName?.takeIf { it.isNotBlank() } ?: conversationId
}


data class ConversationState(
    val conversationId: String,
    val lastReadAt: Long?,
    val isPinned: Boolean,
    val pinnedAt: Long?
)

data class ConversationListUiState(
    val conversations: List<ConversationSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
