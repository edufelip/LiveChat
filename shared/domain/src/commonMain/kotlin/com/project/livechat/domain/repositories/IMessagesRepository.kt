package com.project.livechat.domain.repositories

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import kotlinx.coroutines.flow.Flow

interface IMessagesRepository {
    fun observeConversation(conversationId: String, pageSize: Int = DEFAULT_PAGE_SIZE): Flow<List<Message>>
    suspend fun sendMessage(draft: MessageDraft): Message
    suspend fun syncConversation(conversationId: String, sinceEpochMillis: Long?): List<Message>

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}
