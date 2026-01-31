package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.EmailVerificationSession

interface IEmailVerificationSessionRepository {
    suspend fun loadSession(userId: String): EmailVerificationSession?

    suspend fun saveSession(
        userId: String,
        session: EmailVerificationSession,
    )

    suspend fun clearSession(userId: String)
}
