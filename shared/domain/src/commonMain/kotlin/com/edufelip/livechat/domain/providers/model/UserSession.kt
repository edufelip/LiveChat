package com.edufelip.livechat.domain.providers.model

data class UserSession(
    val userId: String,
    val idToken: String,
    val refreshToken: String? = null,
    val expiresAtEpochMillis: Long? = null,
)
