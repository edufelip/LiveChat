package com.project.livechat.domain.repositories

import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import kotlinx.coroutines.flow.Flow

interface IMessagesRepository {
    fun observeConversation(
        conversationId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Flow<List<Message>>

    suspend fun sendMessage(draft: MessageDraft): Message

    suspend fun syncConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message>

    fun observeConversationSummaries(): Flow<List<ConversationSummary>>

    suspend fun markConversationAsRead(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long? = null,
    )

    suspend fun setConversationPinned(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long? = null,
    )

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}
