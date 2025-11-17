package com.edufelip.livechat.domain.providers

import com.edufelip.livechat.domain.providers.model.UserSession
import kotlinx.coroutines.flow.Flow

interface UserSessionProvider {
    val session: Flow<UserSession?>

    suspend fun refreshSession(forceRefresh: Boolean = false): UserSession?

    fun currentUserId(): String?

    fun currentUserPhone(): String?
}
