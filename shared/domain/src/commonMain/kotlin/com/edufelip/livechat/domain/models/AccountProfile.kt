package com.edufelip.livechat.domain.models

data class AccountProfile(
    val userId: String,
    val displayName: String,
    val statusMessage: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
)
