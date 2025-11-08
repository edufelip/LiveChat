package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsRemoteData {
    fun checkContacts(phoneContacts: List<Contact>): Flow<Contact>

    suspend fun inviteContact(contact: Contact): Boolean
}
