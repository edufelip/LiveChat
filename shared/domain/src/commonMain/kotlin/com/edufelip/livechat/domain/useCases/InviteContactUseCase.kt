package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.InviteChannel
import com.edufelip.livechat.domain.repositories.IContactsRepository

class InviteContactUseCase(
    private val contactsRepository: IContactsRepository,
) {
    suspend operator fun invoke(
        contact: Contact,
        channel: InviteChannel,
    ): InviteContactResult {
        val tracked = contactsRepository.inviteContact(contact)
        val message = buildMessage(contact, channel)
        return InviteContactResult(message = message, tracked = tracked)
    }

    private fun buildMessage(
        contact: Contact,
        channel: InviteChannel,
    ): String {
        val contactName = contact.name.ifBlank { "there" }
        val link = "$INVITE_LINK?channel=${channel.name.lowercase()}"
        return "Hi $contactName! I'm using LiveChat to stay in touch. Download it here: $link"
    }

    companion object {
        private const val INVITE_LINK = "https://www.google.com"
    }
}

data class InviteContactResult(
    val message: String,
    val tracked: Boolean,
)
