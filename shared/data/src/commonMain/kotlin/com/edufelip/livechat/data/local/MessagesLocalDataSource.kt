package com.edufelip.livechat.data.local

import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.data.mappers.toEntity
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.models.ParticipantRole
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.shared.data.database.ConversationStateDao
import com.edufelip.livechat.shared.data.database.ConversationStateEntity
import com.edufelip.livechat.shared.data.database.ConversationSummaryRow
import com.edufelip.livechat.shared.data.database.InboxActionsDao
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.MessagesDao
import com.edufelip.livechat.shared.data.database.ProcessedInboxActionEntity
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
    private val logTag = "COMMCHECK"
    private val messagesDao: MessagesDao = database.messagesDao()
    private val conversationStateDao: ConversationStateDao = database.conversationStateDao()
    private val inboxActionsDao: InboxActionsDao = database.inboxActionsDao()

    override fun observeMessages(
        conversationId: String,
        limit: Int,
    ): Flow<List<Message>> =
        messagesDao.observeMessages(conversationId)
            .map { rows ->
                val mapped = rows.map { it.toDomain() }
                val preview =
                    mapped.takeLast(3).joinToString { msg ->
                        "${msg.id}:${msg.body.take(30)}"
                    }
                println("$logTag: local observeMessages conversation=$conversationId count=${rows.size} tail=$preview")
                if (mapped.size <= limit) mapped else mapped.takeLast(limit)
            }

    override suspend fun upsertMessages(messages: List<Message>) {
        if (messages.isEmpty()) return
        withContext(dispatcher) {
            val grouped = messages.groupBy { it.conversationId }
            println(
                "$logTag: local upsertMessages total=${messages.size} " +
                    grouped.entries.joinToString(prefix = "byConv=", separator = ";") { (conv, msgs) ->
                        "$conv:${msgs.size}"
                    },
            )
            messagesDao.insertAll(messages.map { it.toEntity() })
        }
    }

    override suspend fun insertOutgoingMessage(message: Message) {
        withContext(dispatcher) {
            println(
                "$logTag: local insertOutgoingMessage id=${message.id} conv=${message.conversationId} body=${message.body.take(40)}",
            )
            messagesDao.insert(message.toEntity())
        }
    }

    override suspend fun updateMessageStatusByLocalId(
        localId: String,
        serverId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            println(
                "$logTag: local updateStatusByLocalId localId=$localId serverId=$serverId status=$status",
            )
            messagesDao.updateStatusByLocalId(localId, serverId, status.name)
        }
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus,
    ) {
        withContext(dispatcher) {
            println("$logTag: local updateStatus id=$messageId status=$status")
            messagesDao.updateStatus(messageId, status.name)
        }
    }

    override suspend fun getMessageStatus(messageId: String): MessageStatus? =
        withContext(dispatcher) {
            messagesDao.getStatus(messageId)?.let { runCatching { MessageStatus.valueOf(it) }.getOrNull() }
        }

    override suspend fun latestIncomingMessage(
        conversationId: String,
        currentUserId: String,
    ): Message? =
        withContext(dispatcher) {
            messagesDao.latestIncomingMessage(conversationId, currentUserId)?.toDomain()
        }

    override suspend fun latestTimestamp(conversationId: String): Long? =
        withContext(dispatcher) {
            val ts = messagesDao.latestTimestamp(conversationId)
            println("$logTag: local latestTimestamp conversation=$conversationId ts=$ts")
            ts
        }

    override suspend fun hasProcessedAction(actionId: String): Boolean =
        withContext(dispatcher) {
            inboxActionsDao.hasAction(actionId)
        }

    override suspend fun markActionProcessed(actionId: String) {
        withContext(dispatcher) {
            inboxActionsDao.insert(
                ProcessedInboxActionEntity(
                    actionId = actionId,
                    processedAt = currentEpochMillis(),
                ),
            )
        }
    }

    override suspend fun replaceConversation(
        conversationId: String,
        messages: List<Message>,
    ) {
        withContext(dispatcher) {
            println("$logTag: local replaceConversation conversation=$conversationId count=${messages.size}")
            messagesDao.clearConversation(conversationId)
            if (messages.isNotEmpty()) {
                messagesDao.insertAll(messages.map { it.toEntity() })
            }
        }
    }

    override fun observeConversationSummaries(): Flow<List<ConversationSummary>> =
        messagesDao.observeConversationSummaries()
            .map { rows ->
                println("$logTag: local observeConversationSummaries count=${rows.size}")
                rows.map { it.toConversationSummary() }
            }

    override fun observeParticipant(conversationId: String): Flow<Participant?> =
        conversationStateDao.observeConversationState(conversationId)
            .map { state ->
                println("$logTag: local observeParticipant conversation=$conversationId hasState=${state != null}")
                state?.toParticipant()
            }

    override suspend fun getParticipant(conversationId: String): Participant? =
        withContext(dispatcher) {
            val state = conversationStateDao.getConversationState(conversationId)?.toParticipant()
            println("$logTag: local getParticipant conversation=$conversationId found=${state != null}")
            state
        }

    override suspend fun upsertParticipant(participant: Participant) {
        withContext(dispatcher) {
            println("$logTag: local upsertParticipant conversation=${participant.conversationId} user=${participant.userId}")
            conversationStateDao.upsert(participant.toEntity())
        }
    }

    private fun ConversationSummaryRow.toConversationSummary(): ConversationSummary {
        val statusEnum = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT)
        val contentTypeEnum =
            runCatching { MessageContentType.valueOf(contentType ?: MessageContentType.Text.name) }
                .getOrDefault(MessageContentType.Text)
        val lastMessage =
            Message(
                id = messageId,
                conversationId = conversationId,
                senderId = senderId,
                body = body,
                createdAt = createdAt,
                status = statusEnum,
                contentType = contentTypeEnum,
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

        fun decode(raw: String): Map<String, String> = runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyMap())
    }
}
