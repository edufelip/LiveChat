package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
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
        val localByPhone = localDbContacts.associateBy { it.phoneNo }
        val phoneByPhone = phoneContacts.associateBy { it.phoneNo }

        val toRemove = mutableListOf<Contact>()
        val toInsert = mutableListOf<Contact>()
        val toUpdate = mutableListOf<Contact>()
        val alreadyRegistered = mutableListOf<Contact>()
        val needsValidation = mutableListOf<Contact>()

        localDbContacts.forEach { local ->
            if (local.phoneNo !in phoneByPhone) {
                toRemove.add(local)
            }
        }

        phoneByPhone.values.forEach { phoneContact ->
            val local = localByPhone[phoneContact.phoneNo]
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
                    alreadyRegistered.add(local)
                }
                needsValidation.add(merged.copy(isRegistered = false))
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
                    validatedNumbers.add(registered.phoneNo)
                    repository.updateContacts(listOf(registered))
                }
                .onCompletion {
                    val toMarkUnregistered =
                        needsValidation
                            .filter { it.phoneNo !in validatedNumbers }
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
