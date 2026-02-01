package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter

class BuildContactSyncPlanUseCase(
    private val phoneNumberFormatter: PhoneNumberFormatter,
) {
    operator fun invoke(
        phoneContacts: List<Contact>,
        localDbContacts: List<Contact>,
    ): ContactSyncPlan {
        if (phoneContacts.isEmpty()) {
            return ContactSyncPlan()
        }

        val localByPhone = localDbContacts.associateBy { phoneNumberFormatter.normalize(it.phoneNo) }
        val phoneByPhone = phoneContacts.associateBy { phoneNumberFormatter.normalize(it.phoneNo) }

        val toRemove = mutableListOf<Contact>()
        val toInsert = mutableListOf<Contact>()
        val toUpdate = mutableListOf<Contact>()
        val alreadyRegistered = mutableListOf<Contact>()
        val needsValidation = mutableListOf<Contact>()

        localDbContacts.forEach { local ->
            val normalizedPhone = phoneNumberFormatter.normalize(local.phoneNo)
            if (normalizedPhone !in phoneByPhone) {
                toRemove.add(local)
            }
        }

        phoneByPhone.forEach { (normalizedPhone, phoneContact) ->
            val local = localByPhone[normalizedPhone]
            if (local == null) {
                val newContact = phoneContact.copy(isRegistered = false)
                toInsert.add(newContact)
                needsValidation.add(newContact)
            } else {
                val merged =
                    phoneContact.copy(
                        id = local.id,
                        isRegistered = local.isRegistered,
                        firebaseUid = local.firebaseUid ?: phoneContact.firebaseUid,
                    )
                if (local.name != merged.name ||
                    local.description != merged.description ||
                    local.photo != merged.photo ||
                    local.phoneNo != merged.phoneNo
                ) {
                    toUpdate.add(merged)
                }
                val hasUid = !merged.firebaseUid.isNullOrBlank()
                if (local.isRegistered && hasUid) {
                    alreadyRegistered.add(merged.copy(isRegistered = true))
                } else {
                    needsValidation.add(merged.copy(isRegistered = false))
                }
            }
        }

        return ContactSyncPlan(
            toInsert = toInsert,
            toUpdate = toUpdate,
            toRemove = toRemove,
            alreadyRegistered = alreadyRegistered,
            needsValidation = needsValidation,
        )
    }
}
