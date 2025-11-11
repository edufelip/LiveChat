package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.utils.normalizePhoneNumber

class ResolveConversationIdForContactUseCase {
    operator fun invoke(contact: Contact): String {
        val normalized = normalizePhoneNumber(contact.phoneNo)
        return normalized.takeIf { it.isNotBlank() } ?: contact.phoneNo.trim()
    }
}
