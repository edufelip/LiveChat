package com.project.livechat.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.mappers.insertContact
import com.project.livechat.data.mappers.toDomain
import com.project.livechat.data.mappers.toInsertParams
import com.project.livechat.data.mappers.updateContact
import com.project.livechat.domain.models.Contact
import com.project.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactsLocalDataSource(
    private val database: LiveChatDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IContactsLocalData {

    private val queries = database.contactsQueries

    override fun getLocalContacts(): Flow<List<Contact>> {
        return queries.getAllContacts()
            .asFlow()
            .mapToList(dispatcher)
            .map { contacts -> contacts.map { it.toDomain() } }
    }

    override suspend fun removeContacts(contacts: List<Contact>) {
        queries.transaction {
            contacts.map { it.phoneNo }
                .chunked(CHUNK_SIZE)
                .forEach { phoneChunk ->
                    queries.deleteContactsByPhone(phoneChunk)
                }
        }
    }

    override suspend fun addContact(contact: Contact) {
        database.insertContact(contact.toInsertParams())
    }

    override suspend fun updateContact(contacts: List<Contact>) {
        queries.transaction {
            contacts.forEach { database.updateContact(it) }
        }
    }

    private companion object {
        const val CHUNK_SIZE = 999
    }
}
