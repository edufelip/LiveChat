package com.project.livechat.domain.repositories

import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsRepository {
    fun getLocalContacts(): Flow<List<Contact>>

    fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact>

    suspend fun removeContactsFromLocal(contacts: List<Contact>)

    suspend fun addContactToLocal(contact: Contact)

    suspend fun updateContacts(contacts: List<Contact>)

    suspend fun inviteContact(contact: Contact): Boolean
}
