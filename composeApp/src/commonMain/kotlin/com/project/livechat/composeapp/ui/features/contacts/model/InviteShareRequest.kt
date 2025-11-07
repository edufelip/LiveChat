package com.project.livechat.composeapp.ui.features.contacts.model

import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.InviteChannel

data class InviteShareRequest(
    val contact: Contact,
    val channel: InviteChannel,
    val message: String,
)
