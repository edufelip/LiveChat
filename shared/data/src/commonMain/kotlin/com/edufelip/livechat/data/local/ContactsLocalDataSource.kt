package com.edufelip.livechat.data.local

import com.edufelip.livechat.data.contracts.IContactsLocalData
import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.data.mappers.toEntity
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
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

    override fun observeContact(phoneNumber: String): Flow<Contact?> =
        contactsDao.observeContacts()
            .map { contacts ->
                val normalizedTarget = normalizePhoneNumber(phoneNumber)
                contacts
                    .map { it.toDomain() }
                    .firstOrNull { contact ->
                        contact.firebaseUid?.takeIf { it == phoneNumber } != null ||
                            phonesMatch(contact.phoneNo, phoneNumber, normalizedTarget)
                    }
            }

    override suspend fun getLocalContactsSnapshot(): List<Contact> =
        withContext(dispatcher) {
            contactsDao.getAll().map { it.toDomain() }
        }

    override suspend fun findContact(phoneNumber: String): Contact? =
        withContext(dispatcher) {
            val normalizedTarget = normalizePhoneNumber(phoneNumber)
            contactsDao.getAll()
                .map { it.toDomain() }
                .firstOrNull { contact ->
                    contact.firebaseUid?.takeIf { it == phoneNumber } != null ||
                        phonesMatch(contact.phoneNo, phoneNumber, normalizedTarget)
                }
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
                        firebaseUid = contact.firebaseUid,
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
                if (contact.id > 0) {
                    contactsDao.updateContactById(
                        id = contact.id,
                        name = contact.name,
                        description = contact.description,
                        photo = contact.photo,
                        isRegistered = contact.isRegistered,
                        firebaseUid = contact.firebaseUid,
                        phoneNo = contact.phoneNo,
                    )
                } else {
                    contactsDao.updateContactByPhone(
                        name = contact.name,
                        description = contact.description,
                        photo = contact.photo,
                        isRegistered = contact.isRegistered,
                        firebaseUid = contact.firebaseUid,
                        phoneNo = contact.phoneNo,
                    )
                }
            }
        }
    }

    private companion object {
        const val CHUNK_SIZE = 999

        fun phonesMatch(
            candidate: String,
            raw: String,
            normalizedRaw: String = normalizePhoneNumber(raw),
        ): Boolean {
            val normalizedCandidate = normalizePhoneNumber(candidate)
            if (normalizedCandidate.isNotBlank() && normalizedRaw.isNotBlank()) {
                return normalizedCandidate == normalizedRaw
            }
            return candidate == raw
        }
    }
}
