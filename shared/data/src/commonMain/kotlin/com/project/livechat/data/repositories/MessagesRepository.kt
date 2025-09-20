package com.project.livechat.data.repositories

import com.project.livechat.data.contracts.IMessagesLocalData
import com.project.livechat.data.contracts.IMessagesRemoteData
import com.project.livechat.data.mappers.toPendingMessage
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MessagesRepository(
    private val remoteData: IMessagesRemoteData,
    private val localData: IMessagesLocalData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IMessagesRepository {

    override fun observeConversation(conversationId: String, pageSize: Int): Flow<List<Message>> {
        return localData.observeMessages(conversationId, pageSize)
    }

    override suspend fun sendMessage(draft: MessageDraft): Message {
        return withContext(dispatcher) {
            val resolvedDraft = if (draft.senderId.isNotBlank()) {
                draft
            } else {
                val userId = sessionProvider.currentUserId()
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
                    status = remoteMessage.status
                )
                remoteMessage
            } catch (error: Throwable) {
                localData.updateMessageStatus(
                    messageId = resolvedDraft.localId,
                    status = MessageStatus.ERROR
                )
                throw error
            }
        }
    }

    override suspend fun syncConversation(conversationId: String, sinceEpochMillis: Long?): List<Message> {
        return withContext(dispatcher) {
            val remoteMessages = remoteData.pullHistorical(conversationId, sinceEpochMillis)
            if (sinceEpochMillis == null) {
                localData.replaceConversation(conversationId, remoteMessages)
            } else {
                localData.upsertMessages(remoteMessages)
            }
            remoteMessages
        }
    }
}
