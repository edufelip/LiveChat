package com.edufelip.livechat.domain.models

data class QuietHours(
    val from: String = "22:00",
    val to: String = "07:00",
)

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHours: QuietHours = QuietHours(),
    val showMessagePreview: Boolean = true,
)
