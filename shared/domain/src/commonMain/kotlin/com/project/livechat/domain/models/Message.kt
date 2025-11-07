package com.project.livechat.domain.models

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    ERROR,
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val createdAt: Long,
    val status: MessageStatus,
    val localTempId: String? = null,
    val messageSeq: Long? = null,
    val serverAckAt: Long? = null,
    val contentType: MessageContentType = MessageContentType.Text,
    val ciphertext: String? = null,
    val attachments: List<AttachmentRef> = emptyList(),
    val replyToMessageId: String? = null,
    val threadRootId: String? = null,
    val editedAt: Long? = null,
    val deletedForAllAt: Long? = null,
    val metadata: Map<String, String> = emptyMap(),
)

data class MessageDraft(
    val conversationId: String,
    val senderId: String,
    val body: String,
    val localId: String,
    val createdAt: Long,
    val contentType: MessageContentType = MessageContentType.Text,
    val ciphertext: String? = null,
    val attachments: List<AttachmentRef> = emptyList(),
    val replyToMessageId: String? = null,
    val threadRootId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)
