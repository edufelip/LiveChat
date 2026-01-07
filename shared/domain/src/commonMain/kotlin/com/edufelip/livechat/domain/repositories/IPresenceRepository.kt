package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.PresenceState
import kotlinx.coroutines.flow.Flow

interface IPresenceRepository {
    fun observePresence(userIds: List<String>): Flow<Map<String, PresenceState>>

    suspend fun updateSelfPresence(isOnline: Boolean)
}
