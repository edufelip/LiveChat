package com.project.livechat.domain.useCases

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.repositories.IContactsRepository

class InviteContactUseCase(
    private val contactsRepository: IContactsRepository
) {
    suspend operator fun invoke(contact: Contact): Boolean {
        return contactsRepository.inviteContact(contact)
    }
}
