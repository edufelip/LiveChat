package com.edufelip.livechat.data.local

import com.edufelip.livechat.data.contracts.IContactsLocalData
import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.data.mappers.toEntity
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ContactsLocalDataSource(
    database: LiveChatDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IContactsLocalData {
    private val contactsDao = database.contactsDao()

    override fun getLocalContacts(): Flow<List<Contact>> {
        return contactsDao.observeContacts()
            .map { contacts -> contacts.map { it.toDomain() } }
    }

    override suspend fun removeContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        withContext(dispatcher) {
            contacts.map { it.phoneNo }
                .chunked(CHUNK_SIZE)
                .forEach { phoneChunk ->
                    contactsDao.deleteContactsByPhone(phoneChunk)
                }
        }
    }

    override suspend fun addContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        withContext(dispatcher) {
            contacts.forEach { contact ->
                val inserted = contactsDao.insert(contact.toEntity())
                if (inserted == -1L) {
                    contactsDao.updateContactByPhone(
                        name = contact.name,
                        description = contact.description,
                        photo = contact.photo,
                        isRegistered = contact.isRegistered,
                        phoneNo = contact.phoneNo,
                    )
                }
            }
        }
    }

    override suspend fun updateContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) return
        withContext(dispatcher) {
            contacts.forEach { contact ->
                contactsDao.updateContactByPhone(
                    name = contact.name,
                    description = contact.description,
                    photo = contact.photo,
                    isRegistered = contact.isRegistered,
                    phoneNo = contact.phoneNo,
                )
            }
        }
    }

    private companion object {
        const val CHUNK_SIZE = 999
    }
}
