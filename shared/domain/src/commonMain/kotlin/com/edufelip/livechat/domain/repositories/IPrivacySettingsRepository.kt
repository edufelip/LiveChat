package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import kotlinx.coroutines.flow.Flow

interface IPrivacySettingsRepository {
    fun observeSettings(): Flow<PrivacySettings>

    suspend fun updateInvitePreference(preference: InvitePreference)

    suspend fun updateLastSeenAudience(audience: LastSeenAudience)

    suspend fun updateReadReceipts(enabled: Boolean)

    suspend fun resetSettings()
}
