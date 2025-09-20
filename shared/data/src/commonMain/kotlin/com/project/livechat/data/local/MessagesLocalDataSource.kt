package com.project.livechat.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.project.livechat.data.contracts.IMessagesLocalData
import com.project.livechat.data.mappers.clearConversation
import com.project.livechat.data.mappers.insertMessage
import com.project.livechat.data.mappers.insertMessages
import com.project.livechat.data.mappers.toDomain
import com.project.livechat.data.mappers.toInsertParams
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MessagesLocalDataSource(
    private val database: LiveChatDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IMessagesLocalData {

    private val queries = database.messagesQueries
    private val conversationStateQueries = database.conversation_stateQueries

    override fun observeMessages(conversationId: String, limit: Int): Flow<List<Message>> {
        return queries.getMessagesForConversation(conversationId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows ->
                val mapped = rows.map { it.toDomain() }
                if (mapped.size <= limit) mapped else mapped.takeLast(limit)
            }
    }

    override suspend fun upsertMessages(messages: List<Message>) {
        if (messages.isEmpty()) return
        withContext(dispatcher) {
            database.insertMessages(messages.toInsertParams())
        }
    }

    override suspend fun insertOutgoingMessage(message: Message) {
        withContext(dispatcher) {
            database.insertMessage(message.toInsertParams())
        }
    }

    override suspend fun updateMessageStatusByLocalId(localId: String, serverId: String, status: MessageStatus) {
        withContext(dispatcher) {
            queries.updateMessageStatusByLocalId(
                id = serverId,
                status = status.name,
                local_temp_id = localId
            )
        }
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        withContext(dispatcher) {
            queries.updateMessageStatus(
                status = status.name,
                id = messageId
            )
        }
    }

    override suspend fun latestTimestamp(conversationId: String): Long? {
        return withContext(dispatcher) {
            queries.getLatestMessageTimestamp(conversationId).executeAsOneOrNull()
        }
    }

    override suspend fun replaceConversation(conversationId: String, messages: List<Message>) {
        withContext(dispatcher) {
            database.messagesQueries.transaction {
                database.clearConversation(conversationId)
                database.insertMessages(messages.toInsertParams())
            }
        }
    }

    override fun observeConversationSummaries(): Flow<List<ConversationSummary>> {
        return queries.getConversationSummaries { conversationId, messageId, senderId, body, createdAt, status, lastReadAt, isPinned, pinnedAt, contactName, contactPhoto, unreadCount ->
            val statusEnum = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT)
            val message = Message(
                id = messageId,
                conversationId = conversationId,
                senderId = senderId,
                body = body,
                createdAt = createdAt,
                status = statusEnum,
                localTempId = null
            )
            ConversationSummary(
                conversationId = conversationId,
                contactName = contactName,
                contactPhoto = contactPhoto,
                lastMessage = message,
                unreadCount = unreadCount?.toInt() ?: 0,
                isPinned = (isPinned ?: 0L) != 0L,
                pinnedAt = pinnedAt,
                lastReadAt = lastReadAt
            )
        }
            .asFlow()
            .mapToList(dispatcher)
    }

    override suspend fun markConversationAsRead(conversationId: String, lastReadAt: Long) {
        withContext(dispatcher) {
            conversationStateQueries.insertConversationState(
                conversation_id = conversationId,
                last_read_at = lastReadAt,
                is_pinned = 0,
                pinned_at = null
            )
            conversationStateQueries.updateLastReadAt(
                last_read_at = lastReadAt,
                conversation_id = conversationId
            )
        }
    }

    override suspend fun setConversationPinned(conversationId: String, pinned: Boolean, pinnedAt: Long?) {
        withContext(dispatcher) {
            val pinnedValue = if (pinned) 1L else 0L
            conversationStateQueries.insertConversationState(
                conversation_id = conversationId,
                last_read_at = 0,
                is_pinned = pinnedValue,
                pinned_at = pinnedAt
            )
            conversationStateQueries.updatePinnedState(
                is_pinned = pinnedValue,
                pinned_at = pinnedAt,
                conversation_id = conversationId
            )
        }
    }
}
