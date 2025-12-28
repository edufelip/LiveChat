package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import kotlinx.coroutines.flow.Flow

interface IMessagesRepository {
    fun observeConversation(
        conversationId: String,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Flow<List<Message>>

    suspend fun sendMessage(draft: MessageDraft): Message

    suspend fun deleteMessageLocal(messageId: String)

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
        const val DEFAULT_PAGE_SIZE = 20
    }

    suspend fun ensureConversation(
        conversationId: String,
        peer: ConversationPeer? = null,
    )

    fun observeAllIncomingMessages(): Flow<List<Message>>
}
