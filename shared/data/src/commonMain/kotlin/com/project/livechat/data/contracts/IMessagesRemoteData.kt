package com.project.livechat.data.contracts

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import kotlinx.coroutines.flow.Flow

interface IMessagesRemoteData {
    fun observeConversation(conversationId: String, sinceEpochMillis: Long?): Flow<List<Message>>
    suspend fun sendMessage(draft: MessageDraft): Message
    suspend fun pullHistorical(conversationId: String, sinceEpochMillis: Long?): List<Message>
}
