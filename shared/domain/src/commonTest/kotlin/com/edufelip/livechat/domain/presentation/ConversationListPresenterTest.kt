package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.ConversationFilter
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.SetConversationArchivedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationMutedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationPinnedUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationListPresenterTest {
    private lateinit var scope: TestScope
    private lateinit var repository: FakeMessagesRepository
    private lateinit var participantsRepository: FakeParticipantsRepository
    private lateinit var presenter: ConversationListPresenter

    @AfterTest
    fun tearDown() {
        if (::presenter.isInitialized) {
            presenter.close()
        }
    }

    @Test
    fun setFilterToUnreadOnlyShowsUnreadConversations() =
        runTest {
            val unread = summary(id = "1", unread = 3, pinned = false, timestamp = 3_000)
            val read = summary(id = "2", unread = 0, pinned = true, timestamp = 5_000)
            setUpPresenter(initialSummaries = listOf(unread, read), testScheduler = testScheduler)

            scope.advanceUntilIdle()

            presenter.setFilter(ConversationFilter.Unread)

            val state = presenter.uiState.value
            assertEquals(ConversationFilter.Unread, state.selectedFilter)
            assertTrue(state.conversations.all { it.unreadCount > 0 })
            assertEquals(listOf(unread.conversationId), state.conversations.map { it.conversationId })
        }

    @Test
    fun pinnedFilterReturnsPinnedSortedByRecency() =
        runTest {
            val recentPinned = summary(id = "1", unread = 0, pinned = true, timestamp = 10_000)
            val earlierPinned = summary(id = "2", unread = 0, pinned = true, timestamp = 5_000)
            val unpinned = summary(id = "3", unread = 2, pinned = false, timestamp = 8_000)
            setUpPresenter(
                initialSummaries = listOf(earlierPinned, unpinned, recentPinned),
                testScheduler = testScheduler,
            )

            scope.advanceUntilIdle()

            presenter.setFilter(ConversationFilter.Pinned)

            val state = presenter.uiState.value
            assertEquals(ConversationFilter.Pinned, state.selectedFilter)
            assertTrue(state.conversations.all { it.isPinned })
            assertEquals(
                listOf(recentPinned.conversationId, earlierPinned.conversationId),
                state.conversations.map { it.conversationId },
            )
        }

    @Test
    fun archivedFilterOnlyShowsArchivedConversations() =
        runTest {
            val archived = summary(id = "9", unread = 0, pinned = false, timestamp = 12_000, archived = true)
            val active = summary(id = "8", unread = 1, pinned = false, timestamp = 14_000)
            setUpPresenter(initialSummaries = listOf(archived, active), testScheduler = testScheduler)

            scope.advanceUntilIdle()
            presenter.setFilter(ConversationFilter.Archived)

            val state = presenter.uiState.value
            assertEquals(ConversationFilter.Archived, state.selectedFilter)
            assertEquals(listOf(archived.conversationId), state.conversations.map { it.conversationId })
        }

    @Test
    fun toggleMutedSendsMuteRequest() =
        runTest {
            setUpPresenter(initialSummaries = emptyList(), testScheduler = testScheduler)
            presenter.toggleMuted("conversation-1", muted = true)
            scope.advanceUntilIdle()

            assertTrue(participantsRepository.muteRequests.last().second != null)

            presenter.toggleMuted("conversation-1", muted = false)
            scope.advanceUntilIdle()
            assertEquals(null, participantsRepository.muteRequests.last().second)
        }

    @Test
    fun toggleArchivedSendsArchiveRequest() =
        runTest {
            setUpPresenter(initialSummaries = emptyList(), testScheduler = testScheduler)
            presenter.toggleArchived("conversation-2", archived = true)
            scope.advanceUntilIdle()
            assertEquals(listOf("conversation-2" to true), participantsRepository.archiveRequests)
        }

    @Test
    fun markConversationAsReadUsesLatestSequence() =
        runTest {
            val latest = summary(id = "42", unread = 3, pinned = false, timestamp = 15_000)
            setUpPresenter(initialSummaries = listOf(latest), testScheduler = testScheduler)

            scope.advanceUntilIdle()
            presenter.markConversationAsRead(latest.conversationId)
            scope.advanceUntilIdle()

            assertEquals(
                listOf(Triple(latest.conversationId, latest.lastMessage.createdAt, latest.lastMessage.messageSeq)),
                repository.markReadRequests,
            )
        }

    private fun setUpPresenter(
        initialSummaries: List<ConversationSummary>,
        testScheduler: TestCoroutineScheduler,
    ) {
        repository = FakeMessagesRepository(initialSummaries)
        participantsRepository = FakeParticipantsRepository()
        val dispatcher = StandardTestDispatcher(testScheduler)
        scope = TestScope(dispatcher)
        presenter =
            ConversationListPresenter(
                observeConversationSummaries = ObserveConversationSummariesUseCase(repository),
                markConversationRead = MarkConversationReadUseCase(repository),
                setConversationPinned = SetConversationPinnedUseCase(repository),
                setConversationMuted = SetConversationMutedUseCase(participantsRepository),
                setConversationArchived = SetConversationArchivedUseCase(participantsRepository),
                scope = scope,
            )
    }

    private class FakeMessagesRepository(
        initialSummaries: List<ConversationSummary>,
    ) : IMessagesRepository {
        val summariesFlow = MutableStateFlow(initialSummaries)
        val markReadRequests = mutableListOf<Triple<String, Long, Long?>>()

        override fun observeConversationSummaries(): Flow<List<ConversationSummary>> = summariesFlow

        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> = emptyFlow()

        override suspend fun sendMessage(draft: MessageDraft) = error("Not used in test")

        override suspend fun deleteMessageLocal(messageId: String) = Unit

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

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
        ) {
            // no-op for filter tests
        }

        override suspend fun ensureConversation(
            conversationId: String,
            peer: ConversationPeer?,
        ) = Unit

        override fun observeAllIncomingMessages(): Flow<List<Message>> = emptyFlow()
    }

    private class FakeParticipantsRepository : IConversationParticipantsRepository {
        val muteRequests = mutableListOf<Pair<String, Long?>>()
        val archiveRequests = mutableListOf<Pair<String, Boolean>>()

        override fun observeParticipant(conversationId: String) = emptyFlow<Participant?>()

        override suspend fun recordReadState(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) = Unit

        override suspend fun setPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = Unit

        override suspend fun setMuteUntil(
            conversationId: String,
            muteUntil: Long?,
        ) {
            muteRequests += conversationId to muteUntil
        }

        override suspend fun setArchived(
            conversationId: String,
            archived: Boolean,
        ) {
            archiveRequests += conversationId to archived
        }
    }

    private fun summary(
        id: String,
        unread: Int,
        pinned: Boolean,
        timestamp: Long,
        archived: Boolean = false,
        muted: Boolean = false,
    ): ConversationSummary {
        val message =
            Message(
                id = "msg-$id",
                conversationId = id,
                senderId = "user",
                body = "Body $id",
                createdAt = timestamp,
                status = MessageStatus.SENT,
                messageSeq = timestamp,
            )
        return ConversationSummary(
            conversationId = id,
            contactName = "Contact $id",
            contactPhoto = null,
            lastMessage = message,
            unreadCount = unread,
            isPinned = pinned,
            pinnedAt = if (pinned) timestamp else null,
            lastReadAt = if (unread == 0) timestamp else timestamp - 1_000,
            isMuted = muted,
            muteUntil = if (muted) timestamp + 1_000 else null,
            isArchived = archived,
        )
    }
}
