package com.edufelip.livechat.ui.platform

data class AppVersionInfo(
    val versionName: String,
    val buildNumber: String,
)

expect fun appVersionInfo(): AppVersionInfo
