package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow

class GetLocalContactsUseCase(
    private val repository: IContactsRepository,
) {
    operator fun invoke(): Flow<List<Contact>> = repository.getLocalContacts()
}
