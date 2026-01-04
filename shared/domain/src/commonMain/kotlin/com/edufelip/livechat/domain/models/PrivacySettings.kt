package com.edufelip.livechat.domain.models

enum class InvitePreference {
    Everyone,
    Contacts,
    Nobody,
}

enum class LastSeenAudience {
    Everyone,
    Contacts,
    Nobody,
}

data class PrivacySettings(
    val invitePreference: InvitePreference = InvitePreference.Everyone,
    val lastSeenAudience: LastSeenAudience = LastSeenAudience.Everyone,
    val readReceiptsEnabled: Boolean = true,
    val shareUsageData: Boolean = false,
)
