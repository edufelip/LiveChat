package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class CheckRegisteredContactsUseCase @Inject constructor(
    private val repository: IContactsRepository,
) {
    suspend operator fun invoke(
        phoneContacts: List<Contact>,
        localDbContacts: List<Contact>,
    ): Flow<Contact> {

        val contactsOnlyInLocalDb = mutableListOf<Contact>()
        val contactsOnlyInPhone = mutableListOf<Contact>()
        val innerList = mutableListOf<Contact>()
        val contactsToBeUpdated = mutableListOf<Contact>()

        contactsOnlyInLocalDb.addAll(localDbContacts)
        contactsOnlyInPhone.addAll(phoneContacts)
        localDbContacts.forEach { local ->
            val foundContact =
                phoneContacts.find { it.phoneNo == local.phoneNo } ?: return@forEach
            contactsOnlyInLocalDb.remove(foundContact)
            contactsOnlyInPhone.remove(foundContact)
            innerList.add(foundContact)
            if (foundContact.name != local.name)
                contactsToBeUpdated.add(foundContact)
        }

        if (contactsOnlyInLocalDb.isNotEmpty())
            repository.removeContactsFromLocal(contactsOnlyInLocalDb)
        if (contactsToBeUpdated.isNotEmpty())
            repository.updateContacts(contactsToBeUpdated)

        val alreadyCheckedFlow = flow { innerList.forEach { emit(it) } }
        val checkContactsFlow = repository.checkRegisteredContacts(contactsOnlyInPhone)

        checkContactsFlow.onEach {
            repository.addContactToLocal(it)
        }

        return merge(
            alreadyCheckedFlow, checkContactsFlow
        ).flowOn(Dispatchers.IO)
    }
}