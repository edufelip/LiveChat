package com.project.livechat.data.repositories.mock

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class ContactsRepositoryMock: IContactsRepository {

    private val contactsLocalList = mutableListOf(FAKE_LOCAL_CONTACT_ONE)

    private val registeredContacts = mutableListOf(FAKE_REGISTERED_CONTACT_ONE)

    override fun getLocalContacts(): Flow<List<Contact>> {
        return flowOf(
            contactsLocalList
        )
    }

    override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> {
        return flow {
            for (contact in phoneContacts) {
                if (registeredContacts.firstOrNull{ it.phoneNo == contact.phoneNo } != null) emit(contact)
            }
        }
    }

    override suspend fun removeContactsFromLocal(contacts: List<Contact>) {
        contactsLocalList.removeAll(contacts)
    }

    override suspend fun addContactToLocal(contact: Contact) {
        contactsLocalList.add(contact)
    }

    override suspend fun updateContacts(contacts: List<Contact>) {
        for (c in contacts) {
            val foundContact = contactsLocalList.find {
                it.phoneNo == c.phoneNo
            }
            contactsLocalList.remove(foundContact)
            contactsLocalList.add(c)
        }
    }

    companion object {
        val FAKE_REGISTERED_CONTACT_ONE = Contact(
            name = "Registered Contact 1",
            phoneNo = "+5521988888888",
            description = null,
            photo = null
        )
        val FAKE_REGISTERED_CONTACT_TWO = Contact(
            name = "Registered Contact 2",
            phoneNo = "+5515988888888",
            description = null,
            photo = null
        )
        val FAKE_LOCAL_CONTACT_ONE = Contact(
            name = "Local Contact 1",
            phoneNo = "+5521999999999",
            description = null,
            photo = null
        )
        val FAKE_LOCAL_CONTACT_TWO = Contact(
            name = "Local Contact 2",
            phoneNo = "+5515999999999",
            description = null,
            photo = null
        )
    }
}