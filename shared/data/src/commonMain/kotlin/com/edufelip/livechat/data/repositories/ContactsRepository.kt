package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IContactsLocalData
import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow

class ContactsRepository(
    private val remoteData: IContactsRemoteData,
    private val localData: IContactsLocalData,
) : IContactsRepository {
    override fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact> {
        return remoteData.checkContacts(phoneContacts)
    }

    override fun getLocalContacts(): Flow<List<Contact>> {
        return localData.getLocalContacts()
    }

    override suspend fun removeContactsFromLocal(contacts: List<Contact>) {
        localData.removeContacts(contacts)
    }

    override suspend fun addContactsToLocal(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        localData.addContacts(contacts)
    }

    override suspend fun updateContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        localData.updateContacts(contacts)
    }

    override suspend fun inviteContact(contact: Contact): Boolean {
        return remoteData.inviteContact(contact)
    }
}
