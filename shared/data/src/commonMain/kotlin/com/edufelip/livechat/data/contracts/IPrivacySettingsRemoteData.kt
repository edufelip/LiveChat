package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings

interface IPrivacySettingsRemoteData {
    suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): PrivacySettings?

    suspend fun updateInvitePreference(
        userId: String,
        idToken: String,
        preference: InvitePreference,
    )

    suspend fun updateLastSeenAudience(
        userId: String,
        idToken: String,
        audience: LastSeenAudience,
    )

    suspend fun updateReadReceipts(
        userId: String,
        idToken: String,
        enabled: Boolean,
    )

    suspend fun resetSettings(
        userId: String,
        idToken: String,
    )
}
