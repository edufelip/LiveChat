package com.project.livechat.domain.models

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    ERROR
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val createdAt: Long,
    val status: MessageStatus,
    val localTempId: String? = null
)

data class MessageDraft(
    val conversationId: String,
    val senderId: String,
    val body: String,
    val localId: String,
    val createdAt: Long
)
