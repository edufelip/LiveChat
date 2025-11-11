package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository
import kotlinx.coroutines.flow.Flow

class ObserveContactByPhoneUseCase(
    private val repository: IContactsRepository,
) {
    operator fun invoke(phoneNumber: String): Flow<Contact?> {
        return repository.observeContact(phoneNumber)
    }
}
