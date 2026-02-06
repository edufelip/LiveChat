package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.models.InboxAction
import com.edufelip.livechat.data.models.InboxActionType
import com.edufelip.livechat.data.models.InboxItem
import com.edufelip.livechat.data.repositories.AvatarCacheRepository
import com.edufelip.livechat.data.repositories.MessagesRepository
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.shared.data.database.AvatarCacheDao
import com.edufelip.livechat.shared.data.database.AvatarCacheEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesRepositoryInboxActionTest {
    private val dispatcher = StandardTestDispatcher()
    private val avatarCache = AvatarCacheRepository(FakeAvatarCacheDao(), FakeMediaStorageBridge, dispatcher)

    @Test
    fun actionDeduplicationSkipsRepeatedInboxActions() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            localData.upsertMessages(
                listOf(
                    Message(
                        id = "msg-1",
                        conversationId = "peer-1",
                        senderId = "peer-1",
                        body = "hi",
                        createdAt = 1000L,
                        status = MessageStatus.SENT,
                    ),
                ),
            )
            val remoteData = FakeMessagesRemoteData()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            val job =
                launch {
                    repository.observeConversation("peer-1", 50).collect { }
                }

            val action =
                InboxAction(
                    id = "msg-1_delivered_me",
                    messageId = "msg-1",
                    senderId = "peer-1",
                    receiverId = "me",
                    actionType = InboxActionType.DELIVERED,
                    actionAtMillis = 1010L,
                )
            remoteData.emitInbox(listOf(InboxItem.ActionItem(action)))
            remoteData.emitInbox(listOf(InboxItem.ActionItem(action)))

            advanceUntilIdle()

            assertEquals(1, localData.updateStatusCalls)
            assertTrue(localData.processedActions.contains("msg-1_delivered_me"))

            job.cancel()
        }

    @Test
    fun readActionOverridesDeliveredAndPreventsDowngrade() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            localData.upsertMessages(
                listOf(
                    Message(
                        id = "msg-2",
                        conversationId = "peer-2",
                        senderId = "peer-2",
                        body = "hi",
                        createdAt = 2000L,
                        status = MessageStatus.SENT,
                    ),
                ),
            )
            val remoteData = FakeMessagesRemoteData()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            val job =
                launch {
                    repository.observeConversation("peer-2", 50).collect { }
                }

            val delivered =
                InboxAction(
                    id = "msg-2_delivered_me",
                    messageId = "msg-2",
                    senderId = "peer-2",
                    receiverId = "me",
                    actionType = InboxActionType.DELIVERED,
                    actionAtMillis = 2010L,
                )
            val read =
                InboxAction(
                    id = "msg-2_read_me",
                    messageId = "msg-2",
                    senderId = "peer-2",
                    receiverId = "me",
                    actionType = InboxActionType.READ,
                    actionAtMillis = 2020L,
                )

            remoteData.emitInbox(listOf(InboxItem.ActionItem(delivered)))
            advanceUntilIdle()
            remoteData.emitInbox(listOf(InboxItem.ActionItem(read)))
            advanceUntilIdle()
            remoteData.emitInbox(listOf(InboxItem.ActionItem(delivered)))

            advanceUntilIdle()

            assertEquals(MessageStatus.READ, localData.messages["msg-2"]?.status)
            assertEquals(2, localData.updateStatusCalls)

            job.cancel()
        }

    @Test
    fun markConversationAsReadSendsReadActionForLatestIncomingMessage() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            localData.upsertMessages(
                listOf(
                    Message(
                        id = "incoming-1",
                        conversationId = "peer-3",
                        senderId = "peer-3",
                        body = "hello",
                        createdAt = 3000L,
                        status = MessageStatus.SENT,
                    ),
                    Message(
                        id = "outgoing-1",
                        conversationId = "peer-3",
                        senderId = "me",
                        body = "yo",
                        createdAt = 3100L,
                        status = MessageStatus.SENT,
                    ),
                ),
            )
            val remoteData = FakeMessagesRemoteData()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            repository.markConversationAsRead("peer-3", lastReadAt = 3200L, lastReadSeq = null)

            advanceUntilIdle()

            val action = remoteData.sentActions.singleOrNull()
            assertNotNull(action)
            assertEquals("incoming-1", action.messageId)
            assertEquals("me", action.senderId)
            assertEquals("peer-3", action.receiverId)
            assertEquals(InboxActionType.READ, action.actionType)
        }

    @Test
    fun markConversationAsReadDeletesRemoteMediaAfterRead() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            val temp = File.createTempFile("media", ".jpg").apply { writeText("data") }
            localData.upsertMessages(
                listOf(
                    Message(
                        id = "incoming-media",
                        conversationId = "peer-4",
                        senderId = "peer-4",
                        body = temp.absolutePath,
                        createdAt = 4000L,
                        status = MessageStatus.SENT,
                        contentType = MessageContentType.Image,
                        metadata =
                            mapOf(
                                "remoteUrl" to "https://example.com/media.jpg",
                                "localPath" to temp.absolutePath,
                            ),
                    ),
                ),
            )
            val remoteData = FakeMessagesRemoteData()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            repository.markConversationAsRead("peer-4", lastReadAt = 5000L, lastReadSeq = null)

            advanceUntilIdle()

            assertEquals(listOf("https://example.com/media.jpg"), remoteData.deletedMediaUrls)
            assertTrue(localData.messages["incoming-media"]?.metadata?.containsKey("mediaDeletedAt") == true)
        }

    @Test
    fun actionForMissingMessageIsMarkedProcessed() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            val remoteData = FakeMessagesRemoteData()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            val job =
                launch {
                    repository.observeConversation("peer-4", 50).collect { }
                }

            val delivered =
                InboxAction(
                    id = "missing_delivered_me",
                    messageId = "missing",
                    senderId = "peer-4",
                    receiverId = "me",
                    actionType = InboxActionType.DELIVERED,
                    actionAtMillis = 4000L,
                )
            remoteData.emitInbox(listOf(InboxItem.ActionItem(delivered)))

            advanceUntilIdle()

            assertTrue(localData.processedActions.contains("missing_delivered_me"))
            assertEquals(0, localData.updateStatusCalls)

            job.cancel()
        }

    @Test
    fun sendMessageFailureMarksLocalMessageAsError() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            val remoteData = FakeMessagesRemoteData().apply { shouldFailSend = true }
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = FakeSessionProvider("me"),
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            val draft =
                MessageDraft(
                    conversationId = "peer-5",
                    senderId = "",
                    body = "fail",
                    localId = "local-fail",
                    createdAt = 5000L,
                )

            runCatching { repository.sendMessage(draft) }

            advanceUntilIdle()

            assertEquals(MessageStatus.ERROR, localData.messages["local-fail"]?.status)
        }

    @Test
    fun observeAllIncomingMessagesSubscribesOncePerUserId() =
        runTest(dispatcher) {
            val localData = FakeLocalMessagesData()
            val remoteData = CountingMessagesRemoteData()
            val sessionProvider = MutableSessionProvider()
            val repository =
                MessagesRepository(
                    remoteData = remoteData,
                    localData = localData,
                    sessionProvider = sessionProvider,
                    participantsRepository = FakeParticipantsRepository(),
                    avatarCache = avatarCache,
                    dispatcher = dispatcher,
                )

            val job = launch { repository.observeAllIncomingMessages().collect { } }

            sessionProvider.setSession(UserSession(userId = "user-1", idToken = "token-1"))
            advanceUntilIdle()
            assertEquals(1, remoteData.observeCount)

            sessionProvider.setSession(UserSession(userId = "user-1", idToken = "token-2"))
            advanceUntilIdle()
            assertEquals(1, remoteData.observeCount)

            sessionProvider.setSession(UserSession(userId = "user-2", idToken = "token-3"))
            advanceUntilIdle()
            assertEquals(2, remoteData.observeCount)

            job.cancel()
        }

    private class FakeMessagesRemoteData : IMessagesRemoteData {
        private val inboxFlow = MutableSharedFlow<List<InboxItem>>(replay = 1, extraBufferCapacity = 4)
        val sentActions = mutableListOf<InboxAction>()
        val deletedMediaUrls = mutableListOf<String>()
        val purgedSenders = mutableListOf<String>()
        var shouldFailSend: Boolean = false

        override fun observeConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): Flow<List<InboxItem>> = inboxFlow

        suspend fun emitInbox(items: List<InboxItem>) {
            inboxFlow.emit(items)
        }

        override suspend fun sendMessage(draft: MessageDraft): Message {
            if (shouldFailSend) {
                error("Send failure")
            }
            return Message(
                id = draft.localId,
                conversationId = draft.conversationId,
                senderId = draft.senderId,
                body = draft.body,
                createdAt = draft.createdAt,
                status = MessageStatus.SENT,
            )
        }

        override suspend fun pullHistorical(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

        override suspend fun downloadMediaToLocal(
            remoteUrl: String,
            contentType: MessageContentType,
        ): String = "/tmp/downloaded-${remoteUrl.hashCode()}"

        override suspend fun deleteMedia(remoteUrl: String) {
            deletedMediaUrls += remoteUrl
        }

        override suspend fun sendAction(action: InboxAction) {
            sentActions += action
        }

        override suspend fun ensureConversation(
            conversationId: String,
            userId: String,
            userPhone: String?,
            peer: ConversationPeer?,
        ) = Unit

        override suspend fun purgeInboxMessagesFromSender(senderId: String) {
            purgedSenders += senderId
        }
    }

    private class CountingMessagesRemoteData : IMessagesRemoteData {
        private val inboxFlow = MutableSharedFlow<List<InboxItem>>(replay = 1, extraBufferCapacity = 1)
        var observeCount = 0

        override fun observeConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): Flow<List<InboxItem>> {
            observeCount += 1
            return inboxFlow
        }

        override suspend fun sendMessage(draft: MessageDraft): Message {
            error("Not used in this test")
        }

        override suspend fun pullHistorical(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

        override suspend fun downloadMediaToLocal(
            remoteUrl: String,
            contentType: MessageContentType,
        ): String = "/tmp/ignored"

        override suspend fun deleteMedia(remoteUrl: String) = Unit

        override suspend fun sendAction(action: InboxAction) = Unit

        override suspend fun ensureConversation(
            conversationId: String,
            userId: String,
            userPhone: String?,
            peer: ConversationPeer?,
        ) = Unit

        override suspend fun purgeInboxMessagesFromSender(senderId: String) = Unit
    }

    private class FakeLocalMessagesData : IMessagesLocalData {
        val messages = mutableMapOf<String, Message>()
        val processedActions = mutableSetOf<String>()
        var updateStatusCalls = 0

        override fun observeMessages(
            conversationId: String,
            limit: Int,
        ): Flow<List<Message>> = flowOf(messages.values.filter { it.conversationId == conversationId })

        override suspend fun upsertMessages(messages: List<Message>) {
            messages.forEach { message ->
                this.messages[message.id] = message
            }
        }

        override suspend fun insertOutgoingMessage(message: Message) {
            messages[message.id] = message
        }

        override suspend fun getMessages(conversationId: String): List<Message> =
            messages.values.filter { it.conversationId == conversationId }

        override suspend fun updateMessageStatusByLocalId(
            localId: String,
            serverId: String,
            status: MessageStatus,
        ) {
            val existing = messages[localId] ?: return
            messages.remove(localId)
            messages[serverId] = existing.copy(id = serverId, status = status, localTempId = null)
            updateStatusCalls += 1
        }

        override suspend fun updateMessageStatus(
            messageId: String,
            status: MessageStatus,
        ) {
            val existing = messages[messageId] ?: return
            messages[messageId] = existing.copy(status = status)
            updateStatusCalls += 1
        }

        override suspend fun getMessageStatus(messageId: String): MessageStatus? = messages[messageId]?.status

        override suspend fun downgradeReadStatuses() {
            messages.replaceAll { _, message ->
                if (message.status == MessageStatus.READ) {
                    message.copy(status = MessageStatus.DELIVERED)
                } else {
                    message
                }
            }
        }

        override suspend fun updateMessageBodyAndMetadata(
            messageId: String,
            body: String,
            metadata: Map<String, String>,
        ) {
            val existing = messages[messageId] ?: return
            messages[messageId] = existing.copy(body = body, metadata = metadata)
        }

        override suspend fun updateMessageMetadata(
            messageId: String,
            metadata: Map<String, String>,
        ) {
            val existing = messages[messageId] ?: return
            messages[messageId] = existing.copy(metadata = metadata)
        }

        override suspend fun deleteMessage(messageId: String) {
            messages.entries.removeIf { (key, value) ->
                key == messageId || value.localTempId == messageId
            }
        }

        override suspend fun latestIncomingMessage(
            conversationId: String,
            currentUserId: String,
        ): Message? =
            messages.values
                .filter { it.conversationId == conversationId && it.senderId != currentUserId }
                .maxByOrNull { it.createdAt }

        override suspend fun latestTimestamp(conversationId: String): Long? =
            messages.values
                .filter { it.conversationId == conversationId }
                .maxOfOrNull { it.createdAt }

        override fun observeConversationSummaries(currentUserId: String): Flow<List<ConversationSummary>> = emptyFlow()

        override fun observeParticipant(conversationId: String): Flow<Participant?> = emptyFlow()

        override suspend fun getParticipant(conversationId: String): Participant? = null

        override suspend fun upsertParticipant(participant: Participant) {
        }

        override suspend fun replaceConversation(
            conversationId: String,
            messages: List<Message>,
        ) {
            this.messages.entries.removeIf { it.value.conversationId == conversationId }
            upsertMessages(messages)
        }

        override suspend fun clearConversationData(conversationId: String) {
            this.messages.entries.removeIf { it.value.conversationId == conversationId }
        }

        override suspend fun hasProcessedAction(actionId: String): Boolean = processedActions.contains(actionId)

        override suspend fun markActionProcessed(actionId: String) {
            processedActions += actionId
        }
    }

    private class FakeParticipantsRepository : IConversationParticipantsRepository {
        override fun observeParticipant(conversationId: String): Flow<Participant?> = emptyFlow()

        override suspend fun recordReadState(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) {
        }

        override suspend fun setPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) {
        }

        override suspend fun setMuteUntil(
            conversationId: String,
            muteUntil: Long?,
        ) {
        }

        override suspend fun setArchived(
            conversationId: String,
            archived: Boolean,
        ) {
        }
    }

    private class MutableSessionProvider : UserSessionProvider {
        private val state = MutableStateFlow<UserSession?>(null)

        override val session: Flow<UserSession?> = state

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = state.value

        override fun currentUserId(): String? = state.value?.userId

        override fun currentUserPhone(): String? = state.value?.phoneNumber

        fun setSession(session: UserSession?) {
            state.value = session
        }
    }

    private class FakeSessionProvider(
        private val userId: String,
    ) : UserSessionProvider {
        override val session: Flow<UserSession?> = emptyFlow()

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = null

        override fun currentUserId(): String? = userId

        override fun currentUserPhone(): String? = null
    }

    private object FakeMediaStorageBridge : MediaStorageBridge {
        override suspend fun uploadBytes(
            objectPath: String,
            bytes: ByteArray,
        ): String = "https://example.com/media.jpg"

        override suspend fun downloadBytes(
            remoteUrl: String,
            maxBytes: Long,
        ): ByteArray = byteArrayOf()

        override suspend fun deleteRemote(remoteUrl: String) = Unit
    }

    private class FakeAvatarCacheDao : AvatarCacheDao {
        private val store = mutableMapOf<String, AvatarCacheEntity>()

        override suspend fun get(ownerId: String): AvatarCacheEntity? = store[ownerId]

        override suspend fun upsert(entity: AvatarCacheEntity) {
            store[entity.ownerId] = entity
        }

        override suspend fun delete(ownerId: String) {
            store.remove(ownerId)
        }

        override suspend fun deleteAll() {
            store.clear()
        }
    }
}
