package com.edufelip.livechat.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private class AndroidNotificationPermissionManager(
    private val context: Context,
) : NotificationPermissionManager {
    private var currentState: NotificationPermissionState = resolveState()

    override suspend fun refreshStatus(): NotificationPermissionState {
        currentState = resolveState()
        return currentState
    }

    override fun status(): NotificationPermissionState = currentState

    private fun resolveState(): NotificationPermissionState {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val permissionGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        return if (notificationsEnabled && permissionGranted) {
            NotificationPermissionState.Granted
        } else {
            NotificationPermissionState.Denied
        }
    }
}

@Composable
actual fun rememberNotificationPermissionManager(): NotificationPermissionManager {
    val context = LocalContext.current.applicationContext
    return remember { AndroidNotificationPermissionManager(context) }
}
