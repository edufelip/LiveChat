package com.edufelip.livechat.domain.models

data class BlockedContact(
    val userId: String,
    val displayName: String? = null,
    val phoneNumber: String? = null,
)
