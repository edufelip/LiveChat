package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.mappers.toPendingMessage
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesRepository(
    private val remoteData: IMessagesRemoteData,
    private val localData: IMessagesLocalData,
    private val sessionProvider: UserSessionProvider,
    private val participantsRepository: IConversationParticipantsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IMessagesRepository {
    override fun observeConversation(
        conversationId: String,
        pageSize: Int,
    ): Flow<List<Message>> =
        channelFlow {
            val sinceEpoch = localData.latestTimestamp(conversationId)
            val localJob =
                launch {
                    localData.observeMessages(conversationId, pageSize).collect { messages ->
                        send(messages)
                    }
                }
            val remoteJob =
                launch {
                    remoteData.observeConversation(conversationId, sinceEpoch).collect { remoteMessages ->
                        if (remoteMessages.isNotEmpty()) {
                            localData.upsertMessages(remoteMessages)
                        }
                    }
                }

            awaitClose {
                localJob.cancel()
                remoteJob.cancel()
            }
        }

    override suspend fun sendMessage(draft: MessageDraft): Message {
        return withContext(dispatcher) {
            val resolvedDraft =
                if (draft.senderId.isNotBlank()) {
                    draft
                } else {
                    val userId =
                        sessionProvider.currentUserId()
                            ?: error("User must be authenticated before sending messages.")
                    draft.copy(senderId = userId)
                }
            val pending = resolvedDraft.toPendingMessage(status = MessageStatus.SENDING)
            localData.insertOutgoingMessage(pending)
            try {
                val remoteMessage = remoteData.sendMessage(resolvedDraft)
                localData.updateMessageStatusByLocalId(
                    localId = resolvedDraft.localId,
                    serverId = remoteMessage.id,
                    status = remoteMessage.status,
                )
                localData.upsertMessages(listOf(remoteMessage.copy(localTempId = null)))
                remoteMessage
            } catch (error: Throwable) {
                localData.updateMessageStatus(
                    messageId = resolvedDraft.localId,
                    status = MessageStatus.ERROR,
                )
                throw error
            }
        }
    }

    override suspend fun syncConversation(
        conversationId: String,
        sinceEpochMillis: Long?,
    ): List<Message> {
        return withContext(dispatcher) {
            val remoteMessages = remoteData.pullHistorical(conversationId, sinceEpochMillis)
            if (sinceEpochMillis == null) {
                if (remoteMessages.isNotEmpty()) {
                    localData.replaceConversation(conversationId, remoteMessages)
                }
            } else {
                localData.upsertMessages(remoteMessages)
            }
            remoteMessages
        }
    }

    override fun observeConversationSummaries(): Flow<List<ConversationSummary>> {
        return localData.observeConversationSummaries()
    }

    override suspend fun markConversationAsRead(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long?,
    ) {
        withContext(dispatcher) {
            participantsRepository.recordReadState(conversationId, lastReadAt, lastReadSeq)
        }
    }

    override suspend fun setConversationPinned(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long?,
    ) {
        withContext(dispatcher) {
            participantsRepository.setPinned(conversationId, pinned, pinnedAt)
        }
    }

    override suspend fun ensureConversation(conversationId: String) {
        val userId = sessionProvider.currentUserId()
            ?: error("User must be authenticated before ensuring conversations.")
        remoteData.ensureConversation(conversationId, userId)
    }

}
