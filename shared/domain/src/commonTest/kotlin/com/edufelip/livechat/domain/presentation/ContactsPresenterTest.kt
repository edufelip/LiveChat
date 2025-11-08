package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.InviteChannel
import com.edufelip.livechat.domain.models.InviteHistoryItem
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IInviteHistoryRepository
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.InviteContactUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsPresenterTest {
    @Test
    fun localContactsPopulateUiState() =
        runTest {
            val registered = contact(id = 1, name = "Ava", phone = "+1", registered = true)
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
                        val registered = candidates.first().copy(isRegistered = true)
                        setup.repository.localContactsFlow.value =
                            (setup.repository.localContactsFlow.value + registered)
                                .distinctBy { it.phoneNo }
                        flowOf(registered)
                    }

                setup.presenter.syncContacts(phoneContacts)
                val state =
                    setup.presenter.state
                        .filter { current ->
                            current.validatedContacts.any { it.phoneNo == "+3" && it.isRegistered } &&
                                current.isSyncing.not()
                        }
                        .first()
                assertTrue(
                    state.validatedContacts.any { it.phoneNo == "+3" && it.isRegistered },
                    "Expected validated contact for +3 but found ${state.validatedContacts}",
                )
            } finally {
                setup.presenter.close()
            }
        }

    @Test
    fun inviteContactEmitsShareEvent() =
        runTest {
            val target = contact(id = 5, name = "Eve", phone = "+5", registered = false)
            val setup = createPresenter(initialContacts = emptyList())
            try {
                val event = async { setup.presenter.events.first() }

                setup.presenter.inviteContact(target, InviteChannel.Email)
                setup.scope.advanceUntilIdle()
                setup.scope.advanceUntilIdle()

                val emitted = event.await() as ContactsEvent.ShareInvite
                assertEquals(target, emitted.contact)
                assertEquals(InviteChannel.Email, emitted.channel)
                assertTrue(emitted.message.contains("channel=email"))
                assertTrue(setup.historyRepository.historyFlow.value.any { it.contact == target && it.channel == InviteChannel.Email })
            } finally {
                setup.presenter.close()
            }
        }

    @Test
    fun inviteHistoryUpdatesWithMostRecentFirst() =
        runTest {
            val first = contact(id = 6, name = "Finn", phone = "+6", registered = false)
            val second = contact(id = 7, name = "Gale", phone = "+7", registered = false)
            val setup = createPresenter(initialContacts = emptyList())
            try {
                setup.presenter.inviteContact(first, InviteChannel.Sms)
                setup.scope.advanceUntilIdle()
                setup.scope.advanceUntilIdle()
                setup.presenter.inviteContact(second, InviteChannel.WhatsApp)
                setup.scope.advanceUntilIdle()
                setup.scope.advanceUntilIdle()

                val history = setup.presenter.state.value.inviteHistory
                assertEquals(2, history.size)
                assertEquals(second.phoneNo, history[0].contact.phoneNo)
                assertEquals(first.phoneNo, history[1].contact.phoneNo)
            } finally {
                setup.presenter.close()
            }
        }

    private fun TestScope.createPresenter(initialContacts: List<Contact>): PresenterSetup {
        val repository = FakeContactsRepository()
        repository.localContactsFlow.value = initialContacts
        val historyRepository = FakeInviteHistoryRepository()
        val presenterScope = TestScope(testScheduler)
        val presenter =
            ContactsPresenter(
                getLocalContactsUseCase = GetLocalContactsUseCase(repository),
                checkRegisteredContactsUseCase = CheckRegisteredContactsUseCase(repository),
                inviteContactUseCase = InviteContactUseCase(repository),
                inviteHistoryRepository = historyRepository,
                scope = presenterScope,
            )
        presenterScope.advanceUntilIdle()
        return PresenterSetup(repository, historyRepository, presenter, presenterScope)
    }

    private data class PresenterSetup(
        val repository: FakeContactsRepository,
        val historyRepository: FakeInviteHistoryRepository,
        val presenter: ContactsPresenter,
        val scope: TestScope,
    )

    private class FakeContactsRepository : IContactsRepository {
        val localContactsFlow = MutableStateFlow<List<Contact>>(emptyList())
        var remoteFlowFactory: (List<Contact>) -> Flow<Contact> = { emptyFlow() }
        val invitedContacts = mutableListOf<Contact>()
        var lastChecked: List<Contact>? = null

        override fun getLocalContacts(): Flow<List<Contact>> = localContactsFlow

        override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> =
            remoteFlowFactory(phoneContacts).also { lastChecked = phoneContacts }

        override suspend fun removeContactsFromLocal(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val toRemove = contacts.map { it.phoneNo }.toSet()
            localContactsFlow.value = localContactsFlow.value.filterNot { it.phoneNo in toRemove }
        }

        override suspend fun addContactsToLocal(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val merged =
                (localContactsFlow.value + contacts)
                    .distinctBy { it.phoneNo }
            localContactsFlow.value = merged
        }

        override suspend fun updateContacts(contacts: List<Contact>) {
            if (contacts.isEmpty()) return
            val current = localContactsFlow.value.associateBy { it.phoneNo }.toMutableMap()
            contacts.forEach { contact ->
                current[contact.phoneNo] = contact
            }
            localContactsFlow.value = current.values.toList()
        }

        override suspend fun inviteContact(contact: Contact): Boolean {
            invitedContacts += contact
            return true
        }
    }

    private class FakeInviteHistoryRepository : IInviteHistoryRepository {
        val historyFlow = MutableStateFlow<List<InviteHistoryItem>>(emptyList())

        override val history: Flow<List<InviteHistoryItem>>
            get() = historyFlow

        override suspend fun record(invite: InviteHistoryItem) {
            historyFlow.value = listOf(invite) + historyFlow.value
        }
    }

    private fun contact(
        id: Int,
        name: String,
        phone: String,
        registered: Boolean = false,
    ) = Contact(
        id = id,
        name = name,
        phoneNo = phone,
        description = null,
        photo = null,
        isRegistered = registered,
    )
}
