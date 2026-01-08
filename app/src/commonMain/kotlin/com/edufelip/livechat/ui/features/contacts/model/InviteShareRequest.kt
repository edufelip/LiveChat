package com.edufelip.livechat.ui.features.contacts.model

import com.edufelip.livechat.domain.models.Contact

data class InviteShareRequest(
    val contact: Contact,
    val message: String,
    val chooserTitle: String,
    val unavailableMessage: String,
)
