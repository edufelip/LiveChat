package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.InviteChannel
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InviteContactUseCaseTest {
    @Test
    fun buildsShareMessageAndTracksInvite() =
        runTest {
            val repository = RecordingContactsRepository()
            val useCase = InviteContactUseCase(repository)
            val contact = contact(name = "Sam", phone = "+55")

            val result = useCase(contact, InviteChannel.Sms)

            assertEquals(listOf(contact), repository.invited)
            assertTrue(result.tracked)
            assertTrue(result.message.contains("Sam"))
            assertTrue(result.message.contains("channel=sms"))
        }

    @Test
    fun fallsBackToGenericNameWhenMissing() =
        runTest {
            val repository = RecordingContactsRepository()
            val useCase = InviteContactUseCase(repository)
            val contact = contact(name = "", phone = "+99")

            val result = useCase(contact, InviteChannel.Email)

            assertTrue(result.message.contains("Hi there"))
        }

    private class RecordingContactsRepository : IContactsRepository {
        val invited = mutableListOf<Contact>()
        var inviteReturn = true

        override fun getLocalContacts(): kotlinx.coroutines.flow.Flow<List<Contact>> = throw UnsupportedOperationException()

        override fun checkRegisteredContacts(phoneContacts: List<Contact>): kotlinx.coroutines.flow.Flow<Contact> =
            throw UnsupportedOperationException()

        override suspend fun removeContactsFromLocal(contacts: List<Contact>) = Unit

        override suspend fun addContactsToLocal(contacts: List<Contact>) = Unit

        override suspend fun updateContacts(contacts: List<Contact>) = Unit

        override suspend fun inviteContact(contact: Contact): Boolean {
            invited += contact
            return inviteReturn
        }
    }

    private fun contact(
        name: String,
        phone: String,
    ) = Contact(
        id = 0,
        name = name,
        phoneNo = phone,
        description = null,
        photo = null,
        isRegistered = false,
    )
}
