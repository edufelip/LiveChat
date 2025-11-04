package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.providers.model.UserSession
import com.project.livechat.domain.repositories.IMessagesRepository
import com.project.livechat.domain.useCases.ObserveConversationUseCase
import com.project.livechat.domain.useCases.SendMessageUseCase
import com.project.livechat.domain.useCases.SyncConversationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConversationPresenterTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val repository = FakeMessagesRepository()
    private val sessionProvider = FakeSessionProvider(userId = "tester")

    private val presenter =
        ConversationPresenter(
            observeConversationUseCase = ObserveConversationUseCase(repository),
            sendMessageUseCase = SendMessageUseCase(repository),
            syncConversationUseCase = SyncConversationUseCase(repository),
            userSessionProvider = sessionProvider,
            scope = scope,
        )

    @AfterTest
    fun tearDown() {
        presenter.close()
    }

    @Test
    fun clearError_resetsErrorMessage() =
        scope.runTest {
            presenter.start(conversationId = "conversation")
            advanceUntilIdle()

            repository.failSendMessage = true
            presenter.sendMessage(body = "Hello world")
            advanceUntilIdle()

            assertTrue(presenter.state.value.errorMessage != null)

            presenter.clearError()
            assertNull(presenter.state.value.errorMessage)
        }

    private class FakeMessagesRepository : IMessagesRepository {
        private val messagesState = MutableStateFlow<List<Message>>(emptyList())
        var failSendMessage: Boolean = false

        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> {
            return messagesState
        }

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

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> {
            return messagesState.value
        }

        override fun observeConversationSummaries(): Flow<List<com.project.livechat.domain.models.ConversationSummary>> {
            return emptyFlow()
        }

        override suspend fun markConversationAsRead(
            conversationId: String,
            lastReadAt: Long,
        ) = Unit

        override suspend fun setConversationPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = Unit
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
