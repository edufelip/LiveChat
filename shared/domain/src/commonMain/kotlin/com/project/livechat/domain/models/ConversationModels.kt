package com.project.livechat.domain.models

enum class ConversationType {
    Direct,
    Group,
}

data class Conversation(
    val conversationId: String,
    val type: ConversationType,
    val createdAt: Long,
    val createdBy: String,
    val title: String? = null,
    val lastMessageSeq: Long? = null,
    val lastActivityAt: Long? = null,
    val metadata: Map<String, String> = emptyMap(),
)

enum class ParticipantRole {
    Owner,
    Admin,
    Member,
}

data class Participant(
    val conversationId: String,
    val userId: String,
    val role: ParticipantRole,
    val joinedAt: Long,
    val leftAt: Long? = null,
    val muteUntil: Long? = null,
    val archived: Boolean = false,
    val pinned: Boolean = false,
    val pinnedAt: Long? = null,
    val lastReadAt: Long? = null,
    val lastReadSeq: Long? = null,
    val settings: Map<String, String> = emptyMap(),
)

data class DeliveryReceipt(
    val conversationId: String,
    val messageSeq: Long,
    val userId: String,
    val deliveredAt: Long,
    val readAt: Long? = null,
)

data class Reaction(
    val conversationId: String,
    val messageSeq: Long,
    val userId: String,
    val emoji: String,
    val reactedAt: Long,
)
