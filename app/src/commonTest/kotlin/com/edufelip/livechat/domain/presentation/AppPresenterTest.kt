package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.PresenceState
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPresenceRepository
import com.edufelip.livechat.domain.repositories.IRemoteConfigRepository
import com.edufelip.livechat.domain.useCases.GetLocalContactsSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetWelcomeSeenSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObservePrivacyPolicyUrlUseCase
import com.edufelip.livechat.domain.useCases.ObserveWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.RefreshRemoteConfigUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.useCases.SetWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.UpdateSelfPresenceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppPresenterTest {
    @Test
    fun onboardingFlowReflectsRepositoryUpdates() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = false,
                    initiallyWelcomeSeen = false,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                repository.setOnboardingComplete(true)
                presenterScope.advanceUntilIdle()

                assertTrue(presenter.state.value.isOnboardingComplete)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun initialSnapshotSeedsAppUiState() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = true,
                    initiallyWelcomeSeen = true,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                assertTrue(presenter.state.value.isOnboardingComplete)
                assertTrue(presenter.state.value.hasSeenWelcome)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun onOnboardingFinished_marksOnboardingComplete() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = false,
                    initiallyWelcomeSeen = false,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.onOnboardingFinished()
                presenterScope.advanceUntilIdle()

                assertTrue(repository.currentValue)
                assertTrue(repository.welcomeSeenValue)
                assertTrue(presenter.state.value.isOnboardingComplete)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun onWelcomeFinished_marksWelcomeSeen() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = false,
                    initiallyWelcomeSeen = false,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.onWelcomeFinished()
                presenterScope.advanceUntilIdle()

                assertTrue(repository.welcomeSeenValue)
                assertTrue(presenter.state.value.hasSeenWelcome)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun homeStateTransitionsFollowEvents() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = false,
                    initiallyWelcomeSeen = false,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.selectTab(HomeTab.Calls)
                presenterScope.advanceUntilIdle()
                assertEquals(HomeTab.Calls, presenter.state.value.home.selectedTab)
                assertEquals(null, presenter.state.value.home.activeConversationId)

                presenter.selectTab(HomeTab.Settings)
                presenterScope.advanceUntilIdle()
                assertEquals(HomeTab.Settings, presenter.state.value.home.selectedTab)
                assertEquals(null, presenter.state.value.home.activeConversationId)

                presenter.openConversation("conversation-123")
                presenterScope.advanceUntilIdle()
                assertEquals("conversation-123", presenter.state.value.home.activeConversationId)

                presenter.closeConversation()
                presenterScope.advanceUntilIdle()
                assertEquals(null, presenter.state.value.home.activeConversationId)
            } finally {
                presenter.close()
            }
        }

    @Test
    fun startConversationWithConversationIdUsesProvidedValue() =
        runTest {
            val repository =
                FakeOnboardingRepository(
                    initiallyComplete = true,
                    initiallyWelcomeSeen = true,
                )
            val presenterScope = TestScope(testScheduler)
            val presenter = newPresenter(repository, presenterScope)
            try {
                presenterScope.advanceUntilIdle()
                presenter.startConversationWith(
                    Contact(
                        id = 1,
                        name = "Ava",
                        phoneNo = "+1 (555) 010-2000",
                        isRegistered = true,
                        firebaseUid = "uid-ava",
                    ),
                    conversationId = "conversation-001",
                )
                presenterScope.advanceUntilIdle()
                assertEquals("conversation-001", presenter.state.value.home.activeConversationId)
                assertEquals("Ava", presenter.state.value.home.activeConversationName)
            } finally {
                presenter.close()
            }
        }

    private fun newPresenter(
        repository: FakeOnboardingRepository,
        scope: TestScope,
    ) = AppPresenter(
        observeOnboardingStatus = ObserveOnboardingStatusUseCase(repository),
        observeWelcomeSeen = ObserveWelcomeSeenUseCase(repository),
        observePrivacyPolicyUrl = ObservePrivacyPolicyUrlUseCase(FakeRemoteConfigRepository()),
        observeConversationUseCase = ObserveConversationUseCase(FakeMessagesRepository()),
        observeConversationSummaries = ObserveConversationSummariesUseCase(FakeMessagesRepository()),
        setOnboardingComplete = SetOnboardingCompleteUseCase(repository),
        setWelcomeSeen = SetWelcomeSeenUseCase(repository),
        getOnboardingStatusSnapshot = GetOnboardingStatusSnapshotUseCase(repository),
        getWelcomeSeenSnapshot = GetWelcomeSeenSnapshotUseCase(repository),
        getLocalContactsSnapshot = GetLocalContactsSnapshotUseCase(FakeContactsRepository()),
        refreshRemoteConfig = RefreshRemoteConfigUseCase(FakeRemoteConfigRepository()),
        updateSelfPresence = UpdateSelfPresenceUseCase(FakePresenceRepository()),
        sessionProvider = FakeUserSessionProvider(),
        scope = scope,
    )

    private class FakeOnboardingRepository(
        initiallyComplete: Boolean,
        initiallyWelcomeSeen: Boolean,
    ) : IOnboardingStatusRepository {
        private val state = MutableStateFlow(initiallyComplete)
        private val welcomeState = MutableStateFlow(initiallyWelcomeSeen)

        override val onboardingComplete: Flow<Boolean>
            get() = state

        override val welcomeSeen: Flow<Boolean>
            get() = welcomeState

        override suspend fun setOnboardingComplete(complete: Boolean) {
            state.value = complete
        }

        override suspend fun setWelcomeSeen(seen: Boolean) {
            welcomeState.value = seen
        }

        override fun currentStatus(): Boolean = state.value

        override fun currentWelcomeSeen(): Boolean = welcomeState.value

        val currentValue: Boolean
            get() = state.value

        val welcomeSeenValue: Boolean
            get() = welcomeState.value
    }

    private class FakeMessagesRepository : IMessagesRepository {
        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> = emptyFlow()

        override suspend fun sendMessage(draft: MessageDraft): Message = error("Not used in test")

        override suspend fun deleteMessageLocal(messageId: String) = Unit

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

        override fun observeConversationSummaries(): Flow<List<ConversationSummary>> = flowOf(emptyList())

        override suspend fun markConversationAsRead(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) = Unit

        override suspend fun setConversationPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = Unit

        override suspend fun purgeConversation(conversationId: String) = Unit

        override suspend fun hideReadReceipts() = Unit

        override suspend fun ensureConversation(
            conversationId: String,
            peer: ConversationPeer?,
        ) = Unit

        override fun observeAllIncomingMessages(): Flow<List<Message>> = emptyFlow()
    }

    private class FakeContactsRepository : IContactsRepository {
        override fun getLocalContacts(): Flow<List<Contact>> = MutableStateFlow(emptyList())

        override fun observeContact(phoneNumber: String): Flow<Contact?> = emptyFlow()

        override suspend fun getLocalContactsSnapshot(): List<Contact> = emptyList()

        override suspend fun findContact(phoneNumber: String): Contact? = null

        override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> = emptyFlow()

        override suspend fun removeContactsFromLocal(contacts: List<Contact>) = Unit

        override suspend fun addContactsToLocal(contacts: List<Contact>) = Unit

        override suspend fun updateContacts(contacts: List<Contact>) = Unit

        override suspend fun inviteContact(contact: Contact): Boolean = true
    }

    private class FakePresenceRepository : IPresenceRepository {
        override fun observePresence(userIds: List<String>): Flow<Map<String, PresenceState>> = emptyFlow()

        override suspend fun updateSelfPresence(isOnline: Boolean) = Unit
    }

    private class FakeUserSessionProvider : UserSessionProvider {
        override val session: Flow<UserSession?>
            get() = emptyFlow()

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = null

        override fun currentUserId(): String? = "test-user-id"

        override fun currentUserPhone(): String? = "+1234567890"
    }

    private class FakeRemoteConfigRepository : IRemoteConfigRepository {
        private val privacyPolicyUrl = MutableStateFlow("")

        override fun observePrivacyPolicyUrl(): Flow<String> = privacyPolicyUrl

        override suspend fun refresh() {
        }
    }
}
