package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.models.ParticipantRole
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.SendMessageUseCase
import com.edufelip.livechat.domain.useCases.SyncConversationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationPresenterTest {
    @Test
    fun clearError_resetsErrorMessage() =
        runTest {
            val repository = FakeMessagesRepository()
            val participantsRepository = FakeParticipantsRepository()
            val sessionProvider = FakeSessionProvider(userId = "tester")
            val presenterScope = TestScope(testScheduler)
            val presenter =
                ConversationPresenter(
                    observeConversationUseCase = ObserveConversationUseCase(repository),
                    sendMessageUseCase = SendMessageUseCase(repository),
                    syncConversationUseCase = SyncConversationUseCase(repository),
                    observeParticipantUseCase = ObserveParticipantUseCase(participantsRepository),
                    markConversationReadUseCase = MarkConversationReadUseCase(repository),
                    userSessionProvider = sessionProvider,
                    scope = presenterScope,
                )
            try {
                presenter.start(conversationId = "conversation")
                presenterScope.advanceUntilIdle()

                repository.failSendMessage = true
                presenter.sendMessage(body = "Hello world")
                presenterScope.advanceUntilIdle()

                assertTrue(presenter.state.value.errorMessage != null)

                presenter.clearError()
                presenterScope.advanceUntilIdle()
                assertNull(presenter.state.value.errorMessage)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun start_marksConversationAsReadWhenNewMessageArrives() =
        runTest {
            val repository = FakeMessagesRepository()
            val participantsRepository =
                FakeParticipantsRepository(
                    participant(
                        conversationId = "conversation",
                        userId = "tester",
                        lastReadSeq = 1L,
                        lastReadAt = 1_000L,
                    ),
                )
            val sessionProvider = FakeSessionProvider(userId = "tester")
            val presenterScope = TestScope(testScheduler)
            val presenter =
                ConversationPresenter(
                    observeConversationUseCase = ObserveConversationUseCase(repository),
                    sendMessageUseCase = SendMessageUseCase(repository),
                    syncConversationUseCase = SyncConversationUseCase(repository),
                    observeParticipantUseCase = ObserveParticipantUseCase(participantsRepository),
                    markConversationReadUseCase = MarkConversationReadUseCase(repository),
                    userSessionProvider = sessionProvider,
                    scope = presenterScope,
                )
            try {
                presenter.start(conversationId = "conversation")
                presenterScope.advanceUntilIdle()

                repository.emitMessages(
                    message(
                        id = "m-1",
                        conversationId = "conversation",
                        body = "Hello",
                        createdAt = 5_000L,
                        messageSeq = 5L,
                    ),
                )
                presenterScope.advanceUntilIdle()

                assertEquals(
                    listOf(Triple("conversation", 5_000L, 5L as Long?)),
                    repository.markReadRequests,
                )
            } finally {
                presenter.close()
            }
        }

    @Test
    fun autoReceiptsSkipWhenParticipantIsUpToDate() =
        runTest {
            val repository = FakeMessagesRepository()
            val participantsRepository =
                FakeParticipantsRepository(
                    participant(
                        conversationId = "conversation",
                        userId = "tester",
                        lastReadSeq = 10L,
                        lastReadAt = 10_000L,
                    ),
                )
            val sessionProvider = FakeSessionProvider(userId = "tester")
            val presenterScope = TestScope(testScheduler)
            val presenter =
                ConversationPresenter(
                    observeConversationUseCase = ObserveConversationUseCase(repository),
                    sendMessageUseCase = SendMessageUseCase(repository),
                    syncConversationUseCase = SyncConversationUseCase(repository),
                    observeParticipantUseCase = ObserveParticipantUseCase(participantsRepository),
                    markConversationReadUseCase = MarkConversationReadUseCase(repository),
                    userSessionProvider = sessionProvider,
                    scope = presenterScope,
                )
            try {
                presenter.start(conversationId = "conversation")
                presenterScope.advanceUntilIdle()

                repository.emitMessages(
                    message(
                        id = "m-1",
                        conversationId = "conversation",
                        body = "Old",
                        createdAt = 8_000L,
                        messageSeq = 9L,
                    ),
                )
                presenterScope.advanceUntilIdle()
                assertTrue(repository.markReadRequests.isEmpty())

                repository.emitMessages(
                    message(
                        id = "m-2",
                        conversationId = "conversation",
                        body = "Fresh",
                        createdAt = 12_000L,
                        messageSeq = 12L,
                    ),
                )
                presenterScope.advanceUntilIdle()
                assertEquals(
                    listOf(Triple("conversation", 12_000L, 12L as Long?)),
                    repository.markReadRequests,
                )
            } finally {
                presenter.close()
            }
        }

    private class FakeMessagesRepository : IMessagesRepository {
        private val messagesState = MutableStateFlow<List<Message>>(emptyList())
        val markReadRequests = mutableListOf<Triple<String, Long, Long?>>()
        var failSendMessage: Boolean = false

        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> = messagesState

        override suspend fun sendMessage(draft: MessageDraft): Message {
            if (failSendMessage) {
                throw IllegalStateException("send-error")
            }
            val message =
                Message(
                    id = "id-${messagesState.value.size}",
                    conversationId = draft.conversationId,
                    senderId = draft.senderId,
                    body = draft.body,
                    createdAt = draft.createdAt,
                    status = MessageStatus.SENT,
                )
            messagesState.value = messagesState.value + message
            return message
        }

        fun emitMessages(vararg messages: Message) {
            messagesState.value = messages.toList()
        }

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = messagesState.value

        override fun observeConversationSummaries(): Flow<List<ConversationSummary>> = emptyFlow()

        override suspend fun markConversationAsRead(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) {
            markReadRequests += Triple(conversationId, lastReadAt, lastReadSeq)
        }

        override suspend fun setConversationPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = Unit

    }

    private class FakeParticipantsRepository(
        initialParticipant: Participant? = null,
    ) : IConversationParticipantsRepository {
        private val state = MutableStateFlow(initialParticipant)

        override fun observeParticipant(conversationId: String): Flow<Participant?> = state

        override suspend fun recordReadState(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) {
            val current =
                state.value
                    ?: participant(
                        conversationId = conversationId,
                        userId = "tester",
                    )
            state.value =
                current.copy(
                    lastReadAt = lastReadAt,
                    lastReadSeq = lastReadSeq ?: current.lastReadSeq,
                )
        }

        override suspend fun setPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = Unit

        override suspend fun setMuteUntil(conversationId: String, muteUntil: Long?) = Unit

        override suspend fun setArchived(conversationId: String, archived: Boolean) = Unit
    }

    private class FakeSessionProvider(
        private val userId: String,
    ) : UserSessionProvider {
        private val state = MutableStateFlow(UserSession(userId = userId, idToken = ""))

        override val session: Flow<UserSession?>
            get() = state

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = state.value

        override fun currentUserId(): String? = userId
    }

}

private fun participant(
    conversationId: String,
    userId: String,
    lastReadSeq: Long? = null,
    lastReadAt: Long? = null,
): Participant =
    Participant(
        conversationId = conversationId,
        userId = userId,
        role = ParticipantRole.Member,
        joinedAt = 0L,
        lastReadSeq = lastReadSeq,
        lastReadAt = lastReadAt,
    )

private fun message(
    id: String,
    conversationId: String,
    body: String,
    createdAt: Long,
    messageSeq: Long? = null,
): Message =
    Message(
        id = id,
        conversationId = conversationId,
        senderId = "friend",
        body = body,
        createdAt = createdAt,
        status = MessageStatus.SENT,
        messageSeq = messageSeq,
    )
