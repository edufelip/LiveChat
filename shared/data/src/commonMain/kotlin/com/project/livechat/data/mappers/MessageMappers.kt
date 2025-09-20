package com.project.livechat.data.mappers

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.database.Messages

fun Messages.toDomain(): Message = Message(
    id = id,
    conversationId = conversation_id,
    senderId = sender_id,
    body = body,
    createdAt = created_at,
    status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
    localTempId = local_temp_id
)

fun Message.toInsertParams(): InsertMessageParams = InsertMessageParams(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    body = body,
    createdAt = createdAt,
    status = status,
    localTempId = localTempId
)

fun MessageDraft.toPendingMessage(status: MessageStatus = MessageStatus.SENDING): Message = Message(
    id = localId,
    conversationId = conversationId,
    senderId = senderId,
    body = body,
    createdAt = createdAt,
    status = status,
    localTempId = localId
)

data class InsertMessageParams(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val createdAt: Long,
    val status: MessageStatus,
    val localTempId: String?
)

fun LiveChatDatabase.insertMessage(params: InsertMessageParams) {
    messagesQueries.insertMessage(
        id = params.id,
        conversation_id = params.conversationId,
        sender_id = params.senderId,
        body = params.body,
        created_at = params.createdAt,
        status = params.status.name,
        local_temp_id = params.localTempId
    )
}

fun LiveChatDatabase.insertMessages(messages: List<InsertMessageParams>) {
    messagesQueries.transaction {
        messages.forEach { insertMessage(it) }
    }
}

fun LiveChatDatabase.clearConversation(conversationId: String) {
    messagesQueries.clearConversationMessages(conversationId)
}

fun List<Message>.toInsertParams(): List<InsertMessageParams> = map { it.toInsertParams() }
