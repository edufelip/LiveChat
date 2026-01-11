package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository

class GetLocalContactsSnapshotUseCase(
    private val repository: IContactsRepository,
) {
    suspend operator fun invoke(): List<Contact> = repository.getLocalContactsSnapshot()
}
