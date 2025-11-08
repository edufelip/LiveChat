package com.edufelip.livechat.data.local

import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.data.mappers.toEntity
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.models.ParticipantRole
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.shared.data.database.ConversationStateDao
import com.edufelip.livechat.shared.data.database.ConversationStateEntity
import com.edufelip.livechat.shared.data.database.ConversationSummaryRow
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.MessagesDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class MessagesLocalDataSource(
    database: LiveChatDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IMessagesLocalData {
    private val messagesDao: MessagesDao = database.messagesDao()
    private val conversationStateDao: ConversationStateDao = database.conversationStateDao()

    override fun observeMessages(
        conversationId: String,
        limit: Int,
    ): Flow<List<Message>> =
        messagesDao.observeMessages(conversationId)
            .map { rows ->
                val mapped = rows.map { it.toDomain() }
                if (mapped.size <= limit) mapped else mapped.takeLast(limit)
            }

    override suspend fun upsertMessages(messages: List<Message>) {
        if (messages.isEmpty()) return
        withContext(dispatcher) {
            messagesDao.insertAll(messages.map { it.toEntity() })
        }
    }

    override suspend fun insertOutgoingMessage(message: Message) {
        withContext(dispatcher) {
            messagesDao.insert(message.toEntity())
        }
    }

    override suspend fun updateMessageStatusByLocalId(
        localId: String,
        serverId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            messagesDao.updateStatusByLocalId(localId, serverId, status.name)
        }
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            messagesDao.updateStatus(messageId, status.name)
        }
    }

    override suspend fun latestTimestamp(conversationId: String): Long? =
        withContext(dispatcher) { messagesDao.latestTimestamp(conversationId) }

    override suspend fun replaceConversation(
        conversationId: String,
        messages: List<Message>,
    ) {
        withContext(dispatcher) {
            messagesDao.clearConversation(conversationId)
            if (messages.isNotEmpty()) {
                messagesDao.insertAll(messages.map { it.toEntity() })
            }
        }
    }

    override fun observeConversationSummaries(): Flow<List<ConversationSummary>> =
        messagesDao.observeConversationSummaries()
            .map { rows -> rows.map { it.toConversationSummary() } }

    override fun observeParticipant(conversationId: String): Flow<Participant?> =
        conversationStateDao.observeConversationState(conversationId)
            .map { state -> state?.toParticipant() }

    override suspend fun getParticipant(conversationId: String): Participant? =
        withContext(dispatcher) { conversationStateDao.getConversationState(conversationId)?.toParticipant() }

    override suspend fun upsertParticipant(participant: Participant) {
        withContext(dispatcher) {
            conversationStateDao.upsert(participant.toEntity())
        }
    }

    private fun ConversationSummaryRow.toConversationSummary(): ConversationSummary {
        val statusEnum = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT)
        val lastMessage =
            Message(
                id = messageId,
                conversationId = conversationId,
                senderId = senderId,
                body = body,
                createdAt = createdAt,
                status = statusEnum,
                localTempId = null,
            )
        val muteValue = muteUntil
        val isMuted = muteValue?.let { it > currentEpochMillis() } ?: false
        return ConversationSummary(
            conversationId = conversationId,
            contactName = contactName,
            contactPhoto = contactPhoto,
            lastMessage = lastMessage,
            unreadCount = unreadCount?.toInt() ?: 0,
            isPinned = isPinned ?: false,
            pinnedAt = pinnedAt,
            lastReadAt = lastReadAt,
            isMuted = isMuted,
            muteUntil = muteValue,
            isArchived = archived ?: false,
        )
    }

    private fun ConversationStateEntity.toParticipant(): Participant =
        Participant(
            conversationId = conversationId,
            userId = userId,
            role = runCatching { ParticipantRole.valueOf(role) }.getOrDefault(ParticipantRole.Member),
            joinedAt = joinedAt,
            leftAt = leftAt,
            muteUntil = muteUntil,
            archived = archived,
            pinned = isPinned,
            pinnedAt = pinnedAt,
            lastReadSeq = lastReadSeq,
            lastReadAt = lastReadAt,
            settings = settings?.let { SettingsAdapter.decode(it) } ?: emptyMap(),
        )

    private fun Participant.toEntity(): ConversationStateEntity =
        ConversationStateEntity(
            conversationId = conversationId,
            userId = userId,
            role = role.name,
            joinedAt = joinedAt,
            leftAt = leftAt,
            lastReadAt = lastReadAt ?: 0L,
            lastReadSeq = lastReadSeq,
            muteUntil = muteUntil,
            archived = archived,
            isPinned = pinned,
            pinnedAt = pinnedAt,
            settings = settings.takeIf { it.isNotEmpty() }?.let { SettingsAdapter.encode(it) },
        )

    private object SettingsAdapter {
        private val json = Json { ignoreUnknownKeys = true }
        private val serializer = MapSerializer(String.serializer(), String.serializer())

        fun encode(metadata: Map<String, String>): String = json.encodeToString(serializer, metadata)

        fun decode(raw: String): Map<String, String> =
            runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyMap())
    }
}
