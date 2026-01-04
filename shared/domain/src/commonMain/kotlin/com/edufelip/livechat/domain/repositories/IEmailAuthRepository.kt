package com.edufelip.livechat.domain.repositories

interface IEmailAuthRepository {
    suspend fun sendVerificationEmail(email: String)

    suspend fun isEmailUpdated(expectedEmail: String): Boolean
}
