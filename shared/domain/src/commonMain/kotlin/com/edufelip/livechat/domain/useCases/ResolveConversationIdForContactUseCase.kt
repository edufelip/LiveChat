package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter

class ResolveConversationIdForContactUseCase(
    private val userSessionProvider: UserSessionProvider,
    private val phoneNumberFormatter: PhoneNumberFormatter,
) {
    operator fun invoke(contact: Contact): String {
        val localUid = userSessionProvider.currentUserId()
        val remoteUid = contact.firebaseUid
        if (!localUid.isNullOrBlank() && !remoteUid.isNullOrBlank()) {
            return listOf(localUid, remoteUid).sorted().joinToString(separator = "_")
        }

        val localPhone = userSessionProvider.currentUserPhone()?.let(phoneNumberFormatter::normalize)
        val remotePhone = phoneNumberFormatter.normalize(contact.phoneNo)
        val participants =
            listOfNotNull(
                localPhone?.takeIf { it.isNotBlank() },
                remotePhone.takeIf { it.isNotBlank() },
            )

        if (participants.isNotEmpty()) {
            return participants.sorted().joinToString(separator = "_")
        }

        return contact.phoneNo.trim().takeIf { it.isNotBlank() } ?: localUid.orEmpty()
    }
}
