package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.useCases.ApplyContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.BuildContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.EnsureConversationUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.ResolveConversationIdForContactUseCase
import com.edufelip.livechat.domain.useCases.ValidateContactsUseCase
import com.edufelip.livechat.domain.utils.ContactsSyncSession
import com.edufelip.livechat.domain.utils.ContactsUiStateCache
import com.edufelip.livechat.domain.utils.DefaultPhoneNumberFormatter
import com.edufelip.livechat.domain.utils.MainThreadGuardConfig
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsPresenterTest {
    @BeforeTest
    fun setUp() {
        MainThreadGuardConfig.isEnabled = false
    }

    @AfterTest
    fun tearDown() {
        MainThreadGuardConfig.isEnabled = true
    }

    @Test
    fun localContactsPopulateUiState() =
        runTest {
            val registered = contact(id = 1, name = "Ava", phone = "+1", registered = true, firebaseUid = "uid-ava")
            val unregistered = contact(id = 2, name = "Blake", phone = "+2", registered = false)

            val setup = createPresenter(initialContacts = listOf(registered, unregistered))
            try {
                setup.scope.advanceUntilIdle()
                setup.scope.advanceUntilIdle()

                val state = setup.presenter.state.value
                assertTrue(
                    state.validatedContacts.contains(registered),
                )
                assertTrue(state.localContacts.contains(unregistered))
                assertTrue(state.validatedContacts.none { it.phoneNo == "+2" })
            } finally {
                setup.presenter.close()
            }
        }

    @Test
    fun syncContactsEmitsValidatedResults() =
        runTest {
            val phoneContacts =
                listOf(
                    contact(id = 0, name = "Charlie", phone = "+3"),
                    contact(id = 0, name = "Dana", phone = "+4"),
                )

            val setup = createPresenter(initialContacts = emptyList())
            try {
                setup.repository.remoteFlowFactory =
                    { candidates ->
                        val registered =
                            candidates.first().copy(
                                isRegistered = true,
                                firebaseUid = "uid-${candidates.first().phoneNo}",
                            )
                        setup.repository.localContactsFlow.value =
                            (setup.repository.localContactsFlow.value + registered)
                                .distinctBy { normalizePhoneNumber(it.phoneNo) }
                        flowOf(registered)
                    }

                setup.presenter.syncContacts(phoneContacts)
                val state =
                    setup.presenter.state
                        .filter { current ->
                            current.validatedContacts.any { it.phoneNo == "+3" && it.isRegistered } &&
                                current.isSyncing.not()
                        }.first()
                assertTrue(
                    state.validatedContacts.any { it.phoneNo == "+3" && it.isRegistered },
                    "Expected validated contact for +3 but found ${state.validatedContacts}",
                )
            } finally {
                setup.presenter.close()
            }
        }

    @Test
    fun syncContactsSkipsWhenFingerprintUnchanged() =
        runTest {
            val phoneContacts =
                listOf(
                    contact(id = 0, name = "Eden", phone = "+55 11 9999-9999"),
                    contact(id = 0, name = "Felix", phone = "+55 11 8888-8888"),
                )
            val setup = createPresenter(initialContacts = emptyList())
            try {
                setup.presenter.syncContacts(phoneContacts)
                setup.scope.advanceUntilIdle()
                setup.presenter.syncContacts(phoneContacts)
                setup.scope.advanceUntilIdle()
                assertEquals(
                    1,
                    setup.repository.checkInvocations,
                    "Expected only one remote validation when contacts unchanged",
                )
            } finally {
                setup.presenter.close()
            }
        }

    @Test
    fun setSearchQueryTrimsAndUpdatesState() =
        runTest {
            val setup = createPresenter(initialContacts = emptyList())
            try {
                setup.presenter.setSearchQuery("  Ava  ")
                setup.scope.advanceUntilIdle()
                assertEquals("Ava", setup.presenter.state.value.searchQuery)
            } finally {
                setup.presenter.close()
            }
        }

    private fun TestScope.createPresenter(initialContacts: List<Contact>): PresenterSetup {
        ContactsUiStateCache.clear()
        ContactsSyncSession.markAppOpen()
        val repository = FakeContactsRepository()
        repository.localContactsFlow.value = initialContacts
        val presenterScope = TestScope(testScheduler)
        val sessionProvider = FakeUserSessionProvider()
        val messagesRepository = FakeMessagesRepository()
        val formatter = DefaultPhoneNumberFormatter()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val checkUseCase =
            CheckRegisteredContactsUseCase(
                buildContactSyncPlan = BuildContactSyncPlanUseCase(formatter),
                applyContactSyncPlan = ApplyContactSyncPlanUseCase(repository),
                validateContactsUseCase =
                    ValidateContactsUseCase(
                        repository,
                        dispatcher = dispatcher,
                        phoneNumberFormatter = formatter,
                    ),
                dispatcher = dispatcher,
            )
        val presenter =
            ContactsPresenter(
                getLocalContactsUseCase = GetLocalContactsUseCase(repository),
                checkRegisteredContactsUseCase = checkUseCase,
                resolveConversationIdForContactUseCase = ResolveConversationIdForContactUseCase(sessionProvider, formatter),
                ensureConversationUseCase = EnsureConversationUseCase(messagesRepository),
                phoneNumberFormatter = formatter,
                scope = presenterScope,
            )
        presenterScope.advanceUntilIdle()
        return PresenterSetup(repository, presenter, presenterScope)
    }

    private data class PresenterSetup(
        val repository: FakeContactsRepository,
        val presenter: ContactsPresenter,
        val scope: TestScope,
    )

    private class FakeUserSessionProvider : UserSessionProvider {
        override val session: MutableStateFlow<UserSession?> = MutableStateFlow(null)

        override suspend fun refreshSession(forceRefresh: Boolean): UserSession? = session.value

        override fun currentUserId(): String? = "local-user"

        override fun currentUserPhone(): String? = "+15550101000"
    }

    private class FakeMessagesRepository : IMessagesRepository {
        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> = emptyFlow()

        override suspend fun sendMessage(draft: MessageDraft): Message =
            error("sendMessage should not be called in ContactsPresenter tests")

        override suspend fun deleteMessageLocal(messageId: String) = Unit

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = emptyList()

        override fun observeConversationSummaries(): Flow<List<ConversationSummary>> = emptyFlow()

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
        val localContactsFlow = MutableStateFlow<List<Contact>>(emptyList())
        var remoteFlowFactory: (List<Contact>) -> Flow<Contact> = { emptyFlow() }
        val invitedContacts = mutableListOf<Contact>()
        var lastChecked: List<Contact>? = null
        var checkInvocations: Int = 0

        override fun getLocalContacts(): Flow<List<Contact>> = localContactsFlow

        override fun observeContact(phoneNumber: String): Flow<Contact?> =
            localContactsFlow.map { contacts ->
                contacts.firstOrNull { normalizePhoneNumber(it.phoneNo) == normalizePhoneNumber(phoneNumber) }
            }

        override suspend fun getLocalContactsSnapshot(): List<Contact> = localContactsFlow.value

        override suspend fun findContact(phoneNumber: String): Contact? =
            localContactsFlow.value.firstOrNull { normalizePhoneNumber(it.phoneNo) == normalizePhoneNumber(phoneNumber) }

        override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> =
            remoteFlowFactory(phoneContacts).also {
                lastChecked = phoneContacts
                checkInvocations++
            }

        override suspend fun removeContactsFromLocal(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val toRemove = contacts.map { normalizePhoneNumber(it.phoneNo) }.toSet()
            localContactsFlow.value =
                localContactsFlow.value.filterNot {
                    normalizePhoneNumber(it.phoneNo) in toRemove
                }
        }

        override suspend fun addContactsToLocal(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val merged =
                (localContactsFlow.value + contacts)
                    .distinctBy { normalizePhoneNumber(it.phoneNo) }
            localContactsFlow.value = merged
        }

        override suspend fun updateContacts(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val current =
                localContactsFlow.value.associateBy { normalizePhoneNumber(it.phoneNo) }.toMutableMap()
            contacts.forEach { contact ->
                current[normalizePhoneNumber(contact.phoneNo)] = contact
            }
            localContactsFlow.value = current.values.toList()
        }

        override suspend fun inviteContact(contact: Contact): Boolean {
            invitedContacts += contact
            return true
        }
    }

    private fun contact(
        id: Long,
        name: String,
        phone: String,
        registered: Boolean = false,
        firebaseUid: String? = null,
    ) = Contact(
        id = id,
        name = name,
        phoneNo = phone,
        description = null,
        photo = null,
        isRegistered = registered,
        firebaseUid = firebaseUid ?: if (registered) "uid-$id" else null,
    )
}
