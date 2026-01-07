package com.edufelip.livechat.domain.models

data class PresenceState(
    val userId: String,
    val isOnline: Boolean,
    val lastActiveAt: Long,
)
