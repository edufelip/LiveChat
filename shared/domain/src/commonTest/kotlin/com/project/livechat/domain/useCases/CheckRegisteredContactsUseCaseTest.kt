package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckRegisteredContactsUseCaseTest {
    @Test
    fun syncContactsDiffsAndUpdatesLocalStore() =
        runTest {
            val repository = FakeContactsRepository()
            val useCase = CheckRegisteredContactsUseCase(repository)

            val alice = contact(id = 1, name = "Alice", phone = "+1", registered = true)
            val bobLocal = contact(id = 2, name = "Bob", phone = "+2", registered = false)
            val charlie = contact(id = 3, name = "Charlie", phone = "+3", registered = false)

            val bobPhone = contact(id = 0, name = "Bobby", phone = "+2")
            val dana = contact(id = 0, name = "Dana", phone = "+4")
            val phoneContacts =
                listOf(
                    contact(id = 0, name = "Alice", phone = "+1"),
                    bobPhone,
                    dana,
                )

            repository.remoteFlowFactory =
                { requested ->
                    // Ensure the request includes both Bob (updated) and Dana (new)
                    assertTrue(requested.any { it.phoneNo == "+2" })
                    assertTrue(requested.any { it.phoneNo == "+4" })
                    flow { emit(bobPhone.copy(isRegistered = true)) }
                }

            val emissions =
                useCase(phoneContacts, listOf(alice, bobLocal, charlie)).toList(mutableListOf())

            // Already registered contacts are emitted immediately
            assertTrue(emissions.any { it.phoneNo == "+1" && it.isRegistered })
            // Newly validated contacts surface after remote confirmation
            assertTrue(emissions.any { it.phoneNo == "+2" && it.isRegistered })

            assertEquals(listOf(charlie), repository.removedContacts.single())
            assertEquals(listOf(dana.copy(isRegistered = false)), repository.addedContacts.single())

            val flattenedUpdates = repository.updatedContacts.flatten()
            assertTrue(flattenedUpdates.any { it.phoneNo == "+2" && it.name == "Bobby" })
            assertTrue(flattenedUpdates.any { it.phoneNo == "+2" && it.isRegistered })
            assertTrue(flattenedUpdates.any { it.phoneNo == "+4" && !it.isRegistered })
        }

    private class FakeContactsRepository : IContactsRepository {
        val localContactsFlow = MutableStateFlow<List<Contact>>(emptyList())
        val removedContacts = mutableListOf<List<Contact>>()
        val addedContacts = mutableListOf<List<Contact>>()
        val updatedContacts = mutableListOf<List<Contact>>()
        var remoteFlowFactory: (List<Contact>) -> Flow<Contact> = { emptyFlow() }
        var lastCheckRequest: List<Contact> = emptyList()

        override fun getLocalContacts(): Flow<List<Contact>> = localContactsFlow

        override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> {
            lastCheckRequest = phoneContacts
            return remoteFlowFactory(phoneContacts)
        }

        override suspend fun removeContactsFromLocal(contacts: List<Contact>) {
            removedContacts += contacts
        }

        override suspend fun addContactsToLocal(contacts: List<Contact>) {
            addedContacts += contacts
        }

        override suspend fun updateContacts(contacts: List<Contact>) {
            updatedContacts += contacts
        }

        override suspend fun inviteContact(contact: Contact): Boolean = true
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
