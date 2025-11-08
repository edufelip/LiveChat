package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsRepository {
    fun getLocalContacts(): Flow<List<Contact>>

    fun checkRegisteredContacts(phoneContacts: List<Contact>): Flow<Contact>

    suspend fun removeContactsFromLocal(contacts: List<Contact>)

    suspend fun addContactsToLocal(contacts: List<Contact>)

    suspend fun updateContacts(contacts: List<Contact>)

    suspend fun inviteContact(contact: Contact): Boolean
}
