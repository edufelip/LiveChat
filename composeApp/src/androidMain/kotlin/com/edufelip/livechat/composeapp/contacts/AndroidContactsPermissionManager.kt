package com.edufelip.livechat.composeapp.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

private class AndroidContactsPermissionManager(
    private val context: Context,
) : ContactsPermissionManager {
    private var launcher: ActivityResultLauncher<String>? = null
    private var pendingResult: CompletableDeferred<PermissionState>? = null

    override suspend fun ensurePermission(): PermissionState {
        val current = currentPermissionState()
        if (current is PermissionState.Granted) {
            return current
        }

        pendingResult?.let { deferred ->
            return awaitPendingResult(deferred)
        }

        val activeLauncher = launcher ?: return PermissionState.Denied
        val deferred = CompletableDeferred<PermissionState>()
        pendingResult = deferred

        activeLauncher.launch(Manifest.permission.READ_CONTACTS)

        return awaitPendingResult(deferred)
    }

    override fun status(): PermissionState = currentPermissionState()

    fun registerLauncher(newLauncher: ActivityResultLauncher<String>) {
        launcher = newLauncher
    }

    fun unregisterLauncher(oldLauncher: ActivityResultLauncher<String>) {
        if (launcher === oldLauncher) {
            launcher = null
        }
    }

    fun onPermissionResult(granted: Boolean) {
        val result =
            if (granted) {
                PermissionState.Granted
            } else {
                PermissionState.Denied
            }

        pendingResult?.complete(result)
        pendingResult = null
    }

    private fun currentPermissionState(): PermissionState {
        val granted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS,
            ) == PackageManager.PERMISSION_GRANTED
        return if (granted) PermissionState.Granted else PermissionState.Denied
    }

    private suspend fun awaitPendingResult(deferred: CompletableDeferred<PermissionState>): PermissionState {
        return try {
            deferred.await()
        } finally {
            if (pendingResult === deferred) {
                pendingResult = null
            }
        }
    }
}

@Composable
actual fun rememberContactsPermissionManager(): ContactsPermissionManager {
    val context = LocalContext.current.applicationContext
    val manager = remember { AndroidContactsPermissionManager(context) }
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
