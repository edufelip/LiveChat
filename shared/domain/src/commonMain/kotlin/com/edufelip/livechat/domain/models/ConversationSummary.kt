package com.edufelip.livechat.domain.models

data class ConversationSummary(
    val conversationId: String,
    val contactName: String?,
    val contactPhoto: String?,
    val contactUserId: String? = null,
    val lastMessage: Message,
    val unreadCount: Int,
    val isPinned: Boolean,
    val pinnedAt: Long?,
    val lastReadAt: Long?,
    val isMuted: Boolean = false,
    val muteUntil: Long? = null,
    val isArchived: Boolean = false,
    val isOnline: Boolean = false,
) {
    val displayName: String
        get() = contactName?.takeIf { it.isNotBlank() } ?: conversationId
}

data class ConversationState(
    val conversationId: String,
    val lastReadAt: Long?,
    val isPinned: Boolean,
    val pinnedAt: Long?,
    val isMuted: Boolean,
    val muteUntil: Long?,
    val isArchived: Boolean,
)
