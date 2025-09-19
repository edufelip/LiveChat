package com.project.livechat.data.contracts

import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsLocalData {
    fun getLocalContacts(): Flow<List<Contact>>

    suspend fun removeContacts(contacts: List<Contact>)

    suspend fun addContact(contact: Contact)

    suspend fun updateContact(contacts: List<Contact>)
}
