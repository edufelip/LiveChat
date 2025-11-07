package com.project.livechat.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.project.livechat.data.contracts.IMessagesLocalData
import com.project.livechat.data.mappers.clearConversation
import com.project.livechat.data.mappers.insertMessage
import com.project.livechat.data.mappers.insertMessages
import com.project.livechat.data.mappers.toDomain
import com.project.livechat.data.mappers.toInsertParams
import com.project.livechat.domain.models.ConversationSummary
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.domain.models.Participant
import com.project.livechat.domain.models.ParticipantRole
import com.project.livechat.domain.utils.currentEpochMillis
import com.project.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessagesLocalDataSource(
    private val database: LiveChatDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IMessagesLocalData {
    private val queries = database.messagesQueries
    private val conversationStateQueries = database.conversation_stateQueries

    override fun observeMessages(
        conversationId: String,
        limit: Int,
    ): Flow<List<Message>> {
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

    override suspend fun updateMessageStatusByLocalId(
        localId: String,
        serverId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            queries.updateMessageStatusByLocalId(
                id = serverId,
                status = status.name,
                local_temp_id = localId,
            )
        }
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            queries.updateMessageStatus(
                status = status.name,
                id = messageId,
            )
        }
    }

    override suspend fun latestTimestamp(conversationId: String): Long? {
        return withContext(dispatcher) {
            queries.getLatestMessageTimestamp(conversationId).executeAsOneOrNull()
        }
    }

    override suspend fun replaceConversation(
        conversationId: String,
        messages: List<Message>,
    ) {
        withContext(dispatcher) {
            database.messagesQueries.transaction {
                database.clearConversation(conversationId)
                database.insertMessages(messages.toInsertParams())
            }
        }
    }

    override fun observeConversationSummaries(): Flow<List<ConversationSummary>> {
        return queries.getConversationSummaries {
                conversationId,
                messageId,
                senderId,
                body,
                createdAt,
                status,
                lastReadAt,
                isPinned,
                pinnedAt,
                muteUntil,
                archived,
                contactName,
                contactPhoto,
                unreadCount,
            ->
            val statusEnum = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT)
            val muteValue = muteUntil
            val isMuted = muteValue?.let { it > currentEpochMillis() } ?: false
            val message =
                Message(
                    id = messageId,
                    conversationId = conversationId,
                    senderId = senderId,
                    body = body,
                    createdAt = createdAt,
                    status = statusEnum,
                    localTempId = null,
                )
            ConversationSummary(
                conversationId = conversationId,
                contactName = contactName,
                contactPhoto = contactPhoto,
                lastMessage = message,
                unreadCount = unreadCount?.toInt() ?: 0,
                isPinned = (isPinned ?: 0L) != 0L,
                pinnedAt = pinnedAt,
                lastReadAt = lastReadAt,
                isMuted = isMuted,
                muteUntil = muteValue,
                isArchived = (archived ?: 0L) != 0L,
            )
        }
            .asFlow()
            .mapToList(dispatcher)
    }

    override fun observeParticipant(conversationId: String): Flow<Participant?> =
        conversationStateQueries.observeConversationState(conversationId)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { row -> row?.toParticipant() }

    override suspend fun getParticipant(conversationId: String): Participant? =
        withContext(dispatcher) {
            conversationStateQueries.getConversationState(conversationId).executeAsOneOrNull()?.toParticipant()
        }

    override suspend fun upsertParticipant(participant: Participant) {
        withContext(dispatcher) {
            conversationStateQueries.insertOrReplaceConversationState(
                conversation_id = participant.conversationId,
                user_id = participant.userId,
                role = participant.role.name,
                joined_at = participant.joinedAt,
                left_at = participant.leftAt,
                last_read_at = participant.lastReadAt ?: 0L,
                last_read_seq = participant.lastReadSeq,
                mute_until = participant.muteUntil,
                archived = if (participant.archived) 1L else 0L,
                is_pinned = if (participant.pinned) 1L else 0L,
                pinned_at = participant.pinnedAt,
                settings = participant.settings.takeIf { it.isNotEmpty() }?.let { MetadataAdapter.encode(it) },
            )
        }
    }

    private fun com.project.livechat.shared.data.database.Conversation_state.toParticipant(): Participant =
        Participant(
            conversationId = conversation_id,
            userId = user_id,
            role = runCatching { ParticipantRole.valueOf(role) }.getOrDefault(ParticipantRole.Member),
            joinedAt = joined_at,
            leftAt = left_at,
            muteUntil = mute_until,
            archived = (archived ?: 0L) != 0L,
            pinned = (is_pinned ?: 0L) != 0L,
            pinnedAt = pinned_at,
            lastReadSeq = last_read_seq,
            lastReadAt = last_read_at,
            settings = settings?.let { MetadataAdapter.decode(it) } ?: emptyMap(),
        )
}

private object MetadataAdapter {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = MapSerializer(String.serializer(), String.serializer())

    fun encode(metadata: Map<String, String>): String = json.encodeToString(serializer, metadata)

    fun decode(raw: String): Map<String, String> =
        runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyMap())
}
