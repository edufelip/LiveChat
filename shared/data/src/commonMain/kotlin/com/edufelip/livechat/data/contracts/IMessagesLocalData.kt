package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import kotlinx.coroutines.flow.Flow

interface IMessagesLocalData {
    fun observeMessages(
        conversationId: String,
        limit: Int,
    ): Flow<List<Message>>

    suspend fun upsertMessages(messages: List<Message>)

    suspend fun insertOutgoingMessage(message: Message)

    suspend fun getMessages(conversationId: String): List<Message>

    suspend fun updateMessageStatusByLocalId(
        localId: String,
        serverId: String,
        status: MessageStatus,
    )

    suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus,
    )

    suspend fun getMessageStatus(messageId: String): MessageStatus?

    suspend fun downgradeReadStatuses()

    suspend fun updateMessageBodyAndMetadata(
        messageId: String,
        body: String,
        metadata: Map<String, String>,
    )

    suspend fun updateMessageMetadata(
        messageId: String,
        metadata: Map<String, String>,
    )

    suspend fun deleteMessage(messageId: String)

    suspend fun latestIncomingMessage(
        conversationId: String,
        currentUserId: String,
    ): Message?

    suspend fun latestTimestamp(conversationId: String): Long?

    suspend fun hasProcessedAction(actionId: String): Boolean

    suspend fun markActionProcessed(actionId: String)

    suspend fun replaceConversation(
        conversationId: String,
        messages: List<Message>,
    )

    suspend fun clearConversationData(conversationId: String)

    fun observeConversationSummaries(): Flow<List<ConversationSummary>>

    fun observeParticipant(conversationId: String): Flow<Participant?>

    suspend fun getParticipant(conversationId: String): Participant?

    suspend fun upsertParticipant(participant: Participant)
}
