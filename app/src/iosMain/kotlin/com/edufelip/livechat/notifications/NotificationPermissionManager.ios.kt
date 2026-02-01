package com.edufelip.livechat.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
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

    override suspend fun requestPermission(): NotificationPermissionState {
        val current = fetchStatus()
        if (current is NotificationPermissionState.Granted) {
            currentState = current
            return current
        }

        return suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter
                .currentNotificationCenter()
                .requestAuthorizationWithOptions(
                    options =
                        UNAuthorizationOptionAlert or
                            UNAuthorizationOptionSound or
                            UNAuthorizationOptionBadge,
                    completionHandler = { granted, _ ->
                        val nextState =
                            if (granted) {
                                NotificationPermissionState.Granted
                            } else {
                                NotificationPermissionState.Denied
                            }
                        currentState = nextState
                        if (continuation.isActive) {
                            continuation.resume(nextState)
                        }
                    },
                )
        }
    }

    override fun status(): NotificationPermissionState = currentState

    private suspend fun fetchStatus(): NotificationPermissionState =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter
                .currentNotificationCenter()
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

@Composable
actual fun rememberNotificationPermissionManager(): NotificationPermissionManager = remember { IosNotificationPermissionManager() }
