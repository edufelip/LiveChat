package com.edufelip.livechat.composeapp.ui.features.contacts.model

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.InviteChannel

data class InviteShareRequest(
    val contact: Contact,
    val channel: InviteChannel,
    val message: String,
)
