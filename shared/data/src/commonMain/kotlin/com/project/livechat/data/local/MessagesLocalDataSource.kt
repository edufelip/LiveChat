package com.project.livechat.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.project.livechat.data.contracts.IMessagesLocalData
import com.project.livechat.data.mappers.clearConversation
import com.project.livechat.data.mappers.insertMessage
import com.project.livechat.data.mappers.insertMessages
import com.project.livechat.data.mappers.toDomain
import com.project.livechat.data.mappers.toInsertParams
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
}
