package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsLocalData {
    fun getLocalContacts(): Flow<List<Contact>>

    suspend fun removeContacts(contacts: List<Contact>)

    suspend fun addContacts(contacts: List<Contact>)

    suspend fun updateContacts(contacts: List<Contact>)
}
