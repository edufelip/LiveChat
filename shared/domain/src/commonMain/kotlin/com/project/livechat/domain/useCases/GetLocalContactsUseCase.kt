package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow

class GetLocalContactsUseCase(
    private val repository: IContactsRepository
) {
    operator fun invoke(): Flow<List<Contact>> {
        return repository.getLocalContacts()
    }
}
