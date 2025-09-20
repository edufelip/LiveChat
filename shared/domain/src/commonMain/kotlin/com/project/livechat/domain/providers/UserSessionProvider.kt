package com.project.livechat.domain.providers

import com.project.livechat.domain.providers.model.UserSession
import kotlinx.coroutines.flow.Flow

interface UserSessionProvider {
    val session: Flow<UserSession?>

    suspend fun refreshSession(forceRefresh: Boolean = false): UserSession?

    fun currentUserId(): String?
}
