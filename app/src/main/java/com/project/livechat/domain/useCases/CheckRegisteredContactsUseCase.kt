package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CheckRegisteredContactsUseCase @Inject constructor(
    private val repository: IContactsRepository
) {
    operator fun invoke(phoneContacts: List<Contact>): Flow<Contact> {
        val contactsOnlyInLocalDb = mutableListOf<Contact>()
        val contactsOnlyInPhone = mutableListOf<Contact>()
        val innerList = mutableListOf<Contact>()
        val contactsToBeUpdated = mutableListOf<Contact>()
        repository.getLocalContacts().map { localContactsList ->
            contactsOnlyInLocalDb.addAll(localContactsList)
            contactsOnlyInPhone.addAll(phoneContacts)
            localContactsList.forEach { local ->
                val foundContact =
                    phoneContacts.find { it.phoneNo == local.phoneNo } ?: return@forEach
                contactsOnlyInLocalDb.remove(foundContact)
                contactsOnlyInPhone.remove(foundContact)
                innerList.add(foundContact)
                if (foundContact.name != local.name)
                    contactsToBeUpdated.add(foundContact)
            }

            repository.removeContactsFromLocal(contactsOnlyInLocalDb)
            repository.updateContacts(contactsToBeUpdated)
        }

        val alreadyCheckFlow = flow { innerList.forEach { emit(it) } }
        val checkContactsFlow = repository.checkRegisteredContacts(contactsOnlyInPhone)

        checkContactsFlow.onEach {
            repository.addContactToLocal(it)
        }

        return merge(
            alreadyCheckFlow, checkContactsFlow
        ).flowOn(Dispatchers.IO)
    }
}