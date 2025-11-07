package com.project.livechat.domain.models

import com.project.livechat.domain.utils.currentEpochMillis

enum class InviteChannel(val displayName: String) {
    Sms("SMS"),
    WhatsApp("WhatsApp"),
    Email("Email"),
    Share("Share"),
    ;

    companion object {
        val DefaultOptions = values().toList()
    }
}

data class InviteHistoryItem(
    val id: Long = currentEpochMillis(),
    val contact: Contact,
    val channel: InviteChannel,
    val timestamp: Long = currentEpochMillis(),
)
