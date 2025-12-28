package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.mappers.toPendingMessage
import com.edufelip.livechat.data.models.InboxAction
import com.edufelip.livechat.data.models.InboxActionType
import com.edufelip.livechat.data.models.InboxItem
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
    private val logTag = "COMMCHECK"

    override fun observeConversation(
        conversationId: String,
        pageSize: Int,
    ): Flow<List<Message>> =
        channelFlow {
            val sinceEpoch = localData.latestTimestamp(conversationId)
            val localJob =
                launch {
                    localData.observeMessages(conversationId, pageSize).collect { messages ->
                        println("$logTag: repo observeConversation local conv=$conversationId count=${messages.size}")
                        send(messages)
                    }
                }
            val remoteJob =
                launch {
                    remoteData.observeConversation(conversationId, sinceEpoch)
                        .catch { throwable ->
                            val kind =
                                if (throwable.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                                    "permission"
                                } else {
                                    "error"
                                }
                            println("$logTag: repo remote observe $kind conv=$conversationId error=${throwable.message}")
                            throw throwable
                        }
                        .collect { remoteItems ->
                            val (messages, actions) = splitInboxItems(remoteItems)
                            if (messages.isNotEmpty()) {
                                println("$logTag: repo observeConversation remote conv=$conversationId upserting=${messages.size}")
                                localData.upsertMessages(messages)
                            }
                            if (actions.isNotEmpty()) {
                                println("$logTag: repo observeConversation remote conv=$conversationId actions=${actions.size}")
                                applyActions(actions)
                            }
                        }
                }

            awaitClose {
                localJob.cancel()
                remoteJob.cancel()
            }
        }

    override fun observeAllIncomingMessages(): Flow<List<Message>> =
        channelFlow {
            val currentUser = sessionProvider.currentUserId()
            if (currentUser == null) {
                close()
                return@channelFlow
            }
            println("$logTag: repo observeAllIncomingMessages for user=$currentUser")
            val remoteJob =
                launch {
                    remoteData.observeConversation(currentUser, null)
                        .catch { throwable ->
                            println("$logTag: repo observeAllIncomingMessages error=${throwable.message}")
                        }
                        .collect { remoteItems ->
                            val (messages, actions) = splitInboxItems(remoteItems)
                            if (messages.isNotEmpty()) {
                                println("$logTag: repo observeAllIncomingMessages upserting=${messages.size}")
                                localData.upsertMessages(messages)
                                send(messages)
                            }
                            if (actions.isNotEmpty()) {
                                println("$logTag: repo observeAllIncomingMessages actions=${actions.size}")
                                applyActions(actions)
                            }
                        }
                }
            awaitClose { remoteJob.cancel() }
        }

    override suspend fun sendMessage(draft: MessageDraft): Message {
        return withContext(dispatcher) {
            println("$logTag: repo sendMessage to=${draft.conversationId} localId=${draft.localId}")
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
            val existingStatus = localData.getMessageStatus(resolvedDraft.localId)
            if (existingStatus == MessageStatus.ERROR) {
                localData.updateMessageStatus(
                    messageId = resolvedDraft.localId,
                    status = MessageStatus.SENDING,
                )
            } else {
                localData.insertOutgoingMessage(pending)
            }
            try {
                val remoteMessage = remoteData.sendMessage(resolvedDraft)
                println("$logTag: repo sendMessage delivered id=${remoteMessage.id} conv=${remoteMessage.conversationId}")
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
                    println("$logTag: repo syncConversation replace conv=$conversationId count=${remoteMessages.size}")
                    localData.replaceConversation(conversationId, remoteMessages)
                }
            } else {
                println("$logTag: repo syncConversation upsert conv=$conversationId count=${remoteMessages.size}")
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
            val participant = localData.getParticipant(conversationId)
            val knownReadAt = participant?.lastReadAt ?: 0L
            val knownReadSeq = participant?.lastReadSeq
            val alreadyRead =
                if (lastReadSeq != null && knownReadSeq != null) {
                    lastReadSeq <= knownReadSeq
                } else {
                    lastReadAt <= knownReadAt
                }
            if (alreadyRead) {
                println("$logTag: repo markRead skip conv=$conversationId lastReadAt=$lastReadAt lastReadSeq=$lastReadSeq")
                return@withContext
            }
            println("$logTag: repo markRead conv=$conversationId lastReadAt=$lastReadAt lastReadSeq=$lastReadSeq")
            participantsRepository.recordReadState(conversationId, lastReadAt, lastReadSeq)
            val readerId = sessionProvider.currentUserId() ?: return@withContext
            val latestIncoming = localData.latestIncomingMessage(conversationId, readerId) ?: return@withContext
            val action =
                InboxAction(
                    id = buildActionId(latestIncoming.id, InboxActionType.READ, readerId),
                    messageId = latestIncoming.id,
                    senderId = readerId,
                    receiverId = latestIncoming.senderId,
                    actionType = InboxActionType.READ,
                    actionAtMillis = lastReadAt.takeIf { it > 0 } ?: currentEpochMillis(),
                )
            remoteData.sendAction(action)
        }
    }

    override suspend fun setConversationPinned(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long?,
    ) {
        withContext(dispatcher) {
            println("$logTag: repo setPinned conv=$conversationId pinned=$pinned pinnedAt=$pinnedAt")
            participantsRepository.setPinned(conversationId, pinned, pinnedAt)
        }
    }

    override suspend fun ensureConversation(
        conversationId: String,
        peer: ConversationPeer?,
    ) {
        val userId =
            sessionProvider.currentUserId()
                ?: error("User must be authenticated before ensuring conversations.")
        val userPhone = sessionProvider.currentUserPhone()
        println("$logTag: repo ensureConversation conv=$conversationId peer=$peer")
        remoteData.ensureConversation(conversationId, userId, userPhone, peer)
    }

    private suspend fun applyActions(actions: List<InboxAction>) {
        actions.forEach { action ->
            if (localData.hasProcessedAction(action.id)) {
                return@forEach
            }
            val status = action.actionType.toMessageStatus()
            val current = localData.getMessageStatus(action.messageId)
            if (current == MessageStatus.READ && status == MessageStatus.DELIVERED) {
                localData.markActionProcessed(action.id)
                return@forEach
            }
            localData.updateMessageStatus(action.messageId, status)
            localData.markActionProcessed(action.id)
        }
    }

    private fun splitInboxItems(items: List<InboxItem>): Pair<List<Message>, List<InboxAction>> {
        val messages = mutableListOf<Message>()
        val actions = mutableListOf<InboxAction>()
        items.forEach { item ->
            when (item) {
                is InboxItem.MessageItem -> messages += item.message
                is InboxItem.ActionItem -> actions += item.action
            }
        }
        return messages to actions
    }

    private fun InboxActionType.toMessageStatus(): MessageStatus =
        when (this) {
            InboxActionType.DELIVERED -> MessageStatus.DELIVERED
            InboxActionType.READ -> MessageStatus.READ
        }

    private fun buildActionId(
        messageId: String,
        actionType: InboxActionType,
        actorId: String,
    ): String = "${messageId}_${actionType.name.lowercase()}_$actorId"
}
