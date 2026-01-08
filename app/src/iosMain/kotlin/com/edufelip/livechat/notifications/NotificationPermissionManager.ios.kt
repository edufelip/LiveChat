package com.edufelip.livechat.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

private class IosNotificationPermissionManager : NotificationPermissionManager {
    private var currentState: NotificationPermissionState = NotificationPermissionState.Denied

    override suspend fun refreshStatus(): NotificationPermissionState {
        currentState = fetchStatus()
        return currentState
    }

    override fun status(): NotificationPermissionState = currentState

    private suspend fun fetchStatus(): NotificationPermissionState {
        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    val status = settings?.authorizationStatus
                    val nextState =
                        when (status) {
                            UNAuthorizationStatusAuthorized,
                            UNAuthorizationStatusProvisional,
                            UNAuthorizationStatusEphemeral,
                            -> NotificationPermissionState.Granted
                            UNAuthorizationStatusDenied,
                            UNAuthorizationStatusNotDetermined,
                            null,
                            -> NotificationPermissionState.Denied
                            else -> NotificationPermissionState.Denied
                        }
                    if (continuation.isActive) {
                        continuation.resume(nextState)
                    }
                }
        }
    }
}

@Composable
actual fun rememberNotificationPermissionManager(): NotificationPermissionManager {
    return remember { IosNotificationPermissionManager() }
}
