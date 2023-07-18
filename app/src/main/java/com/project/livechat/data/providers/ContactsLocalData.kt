package com.project.livechat.data.providers

import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.db.ContactDao
import com.project.livechat.data.db.entities.ContactRoom
import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactsLocalData @Inject constructor(
    private val dao: ContactDao
) : IContactsLocalData {

    override fun getLocalContacts(): Flow<List<Contact>> {
        val contacts = dao.getAllContacts()
        return contacts.map { list ->
            list.map { it.toContact() }
        }
    }

    override suspend fun removeContacts(contacts: List<Contact>) {
        return dao.deleteContacts(
            contacts.map { it.phoneNo }
        )
    }

    override suspend fun addContact(contact: Contact) {
        val contactReq = ContactRoom.fromContact(contact)
        return dao.insertContact(contactReq)
    }

    override suspend fun updateContact(contacts: List<Contact>) {
        contacts.forEach {
            val (name, phoneNo, description, photo) = it
            dao.updateContact(name, phoneNo, description ?: "", photo ?: "")
        }
    }
}
