package com.edufelip.livechat.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

private class AndroidNotificationPermissionManager(
    private val context: Context,
) : NotificationPermissionManager {
    private var currentState: NotificationPermissionState = resolveState()
    private var launcher: ActivityResultLauncher<String>? = null
    private var pendingResult: CompletableDeferred<NotificationPermissionState>? = null

    override suspend fun refreshStatus(): NotificationPermissionState {
        currentState = resolveState()
        return currentState
    }

    override suspend fun requestPermission(): NotificationPermissionState {
        val current = resolveState()
        if (current is NotificationPermissionState.Granted) {
            currentState = current
            return current
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            currentState = current
            return current
        }

        pendingResult?.let { deferred ->
            return awaitPendingResult(deferred)
        }

        val activeLauncher = launcher ?: return current
        val deferred = CompletableDeferred<NotificationPermissionState>()
        pendingResult = deferred

        activeLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        return awaitPendingResult(deferred)
    }

    override fun status(): NotificationPermissionState = currentState

    fun registerLauncher(newLauncher: ActivityResultLauncher<String>) {
        launcher = newLauncher
    }

    fun unregisterLauncher(oldLauncher: ActivityResultLauncher<String>) {
        if (launcher === oldLauncher) {
            launcher = null
        }
    }

    fun onPermissionResult(granted: Boolean) {
        currentState = resolveState(grantedOverride = granted)
        pendingResult?.complete(currentState)
        pendingResult = null
    }

    private fun resolveState(): NotificationPermissionState = resolveState(grantedOverride = null)

    private fun resolveState(grantedOverride: Boolean?): NotificationPermissionState {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val permissionGranted =
            grantedOverride
                ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    private suspend fun awaitPendingResult(deferred: CompletableDeferred<NotificationPermissionState>): NotificationPermissionState =
        try {
            deferred.await()
        } finally {
            if (pendingResult === deferred) {
                pendingResult = null
            }
        }
}

@Composable
actual fun rememberNotificationPermissionManager(): NotificationPermissionManager {
    val context = LocalContext.current.applicationContext
    val manager = remember { AndroidNotificationPermissionManager(context) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            manager.onPermissionResult(granted)
        }

    DisposableEffect(launcher) {
        manager.registerLauncher(launcher)
        onDispose {
            manager.unregisterLauncher(launcher)
        }
    }

    return manager
}
