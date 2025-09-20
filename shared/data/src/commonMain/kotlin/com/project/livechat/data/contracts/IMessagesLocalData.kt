package com.project.livechat.data.contracts

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageStatus
import kotlinx.coroutines.flow.Flow

interface IMessagesLocalData {
    fun observeMessages(conversationId: String, limit: Int): Flow<List<Message>>
    suspend fun upsertMessages(messages: List<Message>)
    suspend fun insertOutgoingMessage(message: Message)
    suspend fun updateMessageStatusByLocalId(localId: String, serverId: String, status: MessageStatus)
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    suspend fun latestTimestamp(conversationId: String): Long?
    suspend fun replaceConversation(conversationId: String, messages: List<Message>)
}
