package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter

class ResolveConversationIdForContactUseCase(
    private val userSessionProvider: UserSessionProvider,
    private val phoneNumberFormatter: PhoneNumberFormatter,
) {
    operator fun invoke(contact: Contact): String {
        // Conversation ID must be the recipient's identifier so their inbox lives under their UID.
        val recipientUid = contact.firebaseUid?.takeIf { it.isNotBlank() }
        if (recipientUid != null) return recipientUid

        // Fallback to normalized phone as a recipient key if UID is unavailable.
        val normalizedPhone = phoneNumberFormatter.normalize(contact.phoneNo)
        if (normalizedPhone.isNotBlank()) return normalizedPhone

        // Last resort: raw phone or empty.
        return contact.phoneNo.trim()
    }
}
