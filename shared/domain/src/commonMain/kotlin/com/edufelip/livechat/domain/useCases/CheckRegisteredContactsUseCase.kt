package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

class CheckRegisteredContactsUseCase(
    private val repository: IContactsRepository,
) {
    suspend operator fun invoke(
        phoneContacts: List<Contact>,
        localDbContacts: List<Contact>,
    ): Flow<Contact> {
        val localByPhone = localDbContacts.associateBy { normalizePhoneNumber(it.phoneNo) }
        val phoneByPhone = phoneContacts.associateBy { normalizePhoneNumber(it.phoneNo) }

        val toRemove = mutableListOf<Contact>()
        val toInsert = mutableListOf<Contact>()
        val toUpdate = mutableListOf<Contact>()
        val alreadyRegistered = mutableListOf<Contact>()
        val needsValidation = mutableListOf<Contact>()

        localDbContacts.forEach { local ->
            val normalizedPhone = normalizePhoneNumber(local.phoneNo)
            if (normalizedPhone !in phoneByPhone) {
                toRemove.add(local)
            }
        }

        phoneByPhone.forEach { (normalizedPhone, phoneContact) ->
            val local = localByPhone[normalizedPhone]
            if (local == null) {
                val newContact = phoneContact.copy(isRegistered = false)
                toInsert.add(newContact)
                needsValidation.add(newContact)
            } else {
                val merged =
                    phoneContact.copy(
                        id = local.id,
                        isRegistered = local.isRegistered,
                    )
                if (local.name != merged.name ||
                    local.description != merged.description ||
                    local.photo != merged.photo
                ) {
                    toUpdate.add(merged)
                }
                if (local.isRegistered) {
                    alreadyRegistered.add(merged.copy(isRegistered = true))
                } else {
                    needsValidation.add(merged.copy(isRegistered = false))
                }
            }
        }

        if (toRemove.isNotEmpty()) {
            repository.removeContactsFromLocal(toRemove)
        }
        if (toInsert.isNotEmpty()) {
            repository.addContactsToLocal(toInsert)
        }
        if (toUpdate.isNotEmpty()) {
            repository.updateContacts(toUpdate)
        }

        val alreadyCheckedFlow = flow { alreadyRegistered.forEach { emit(it) } }
        val validatedNumbers = mutableSetOf<String>()
        val checkContactsFlow =
            repository.checkRegisteredContacts(needsValidation)
                .onEach { validated ->
                    val registered = validated.copy(isRegistered = true)
                    validatedNumbers.add(normalizePhoneNumber(registered.phoneNo))
                    repository.updateContacts(listOf(registered))
                }
                .onCompletion {
                    val toMarkUnregistered =
                        needsValidation
                            .filter { normalizePhoneNumber(it.phoneNo) !in validatedNumbers }
                            .map { it.copy(isRegistered = false) }
                    if (toMarkUnregistered.isNotEmpty()) {
                        repository.updateContacts(toMarkUnregistered)
                    }
                }
                .flowOn(Dispatchers.Default)

        return merge(
            alreadyCheckedFlow,
            checkContactsFlow,
        ).flowOn(Dispatchers.Default)
    }
}
