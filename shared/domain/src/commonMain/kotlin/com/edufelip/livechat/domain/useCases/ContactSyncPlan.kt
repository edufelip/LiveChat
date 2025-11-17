package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact

data class ContactSyncPlan(
    val toInsert: List<Contact> = emptyList(),
    val toUpdate: List<Contact> = emptyList(),
    val toRemove: List<Contact> = emptyList(),
    val alreadyRegistered: List<Contact> = emptyList(),
    val needsValidation: List<Contact> = emptyList(),
)
