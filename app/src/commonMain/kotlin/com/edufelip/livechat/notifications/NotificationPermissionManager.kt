package com.edufelip.livechat.notifications

import androidx.compose.runtime.Composable

interface NotificationPermissionManager {
    suspend fun refreshStatus(): NotificationPermissionState

    suspend fun requestPermission(): NotificationPermissionState

    fun status(): NotificationPermissionState
}

sealed interface NotificationPermissionState {
    object Granted : NotificationPermissionState

    object Denied : NotificationPermissionState
}

@Composable
expect fun rememberNotificationPermissionManager(): NotificationPermissionManager
