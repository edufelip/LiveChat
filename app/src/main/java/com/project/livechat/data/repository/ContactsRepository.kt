package com.project.livechat.data.repository

import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val remoteData: IContactsRemoteData,
    private val localData: IContactsLocalData
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

    override suspend fun addContactToLocal(contact: Contact) {
        localData.addContact(contact)
    }

    override suspend fun updateContacts(contacts: List<Contact>) {
        localData.updateContact(contacts)
    }
}