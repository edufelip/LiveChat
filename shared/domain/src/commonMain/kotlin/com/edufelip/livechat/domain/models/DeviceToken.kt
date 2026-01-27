package com.edufelip.livechat.domain.models

enum class DevicePlatform {
    Android,
    Ios,
}

data class DeviceToken(
    val deviceId: String,
    val fcmToken: String,
    val platform: DevicePlatform,
    val lastUpdatedAt: Long,
    val appVersion: String? = null,
    val isActive: Boolean = true,
)

data class DeviceTokenRegistration(
    val deviceId: String,
    val fcmToken: String,
    val platform: DevicePlatform,
    val appVersion: String? = null,
)
