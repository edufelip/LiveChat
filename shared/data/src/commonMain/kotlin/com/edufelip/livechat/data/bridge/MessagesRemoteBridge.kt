package com.edufelip.livechat.data.bridge

interface MessagesRemoteBridge {
    fun startListening(
        recipientId: String,
        listener: MessagesRemoteListener,
    ): String

    fun stopListening(token: String)

    suspend fun fetchMessages(recipientId: String): List<TransportMessagePayload>

    suspend fun sendMessage(
        recipientId: String,
        documentId: String,
        payload: TransportMessagePayload,
    ): String

    suspend fun deleteMessage(
        recipientId: String,
        documentId: String,
    )

    suspend fun ensureConversation(conversationId: String)
}

interface MessagesRemoteListener {
    fun onMessages(messages: List<TransportMessagePayload>)

    fun onError(message: String)
}

data class TransportMessagePayload(
    val id: String,
    val senderId: String? = null,
    val receiverId: String? = null,
    val createdAtMillis: Long? = null,
    val createdAtServerMillis: Long? = null,
    val content: String? = null,
    val actionType: String? = null,
    val messageId: String? = null,
    val actionMessageId: String? = null,
    val contentType: String? = null,
)
