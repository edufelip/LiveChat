package com.project.livechat.data.contracts

import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.flow.Flow

interface IContactsRemoteData {
    fun checkContacts(phoneContacts: List<Contact>): Flow<Contact>
}
