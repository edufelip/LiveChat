package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.PresenceState

interface IPresenceRemoteData {
    suspend fun updatePresence(
        userId: String,
        idToken: String,
        isOnline: Boolean,
        lastActiveAt: Long,
    )

    suspend fun fetchPresence(
        userIds: List<String>,
        idToken: String,
    ): Map<String, PresenceState>
}
