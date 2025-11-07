package com.project.livechat.composeapp.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class IosContactsPermissionManager : ContactsPermissionManager {
    override suspend fun ensurePermission(): PermissionState = PermissionState.Granted

    override fun status(): PermissionState = PermissionState.Granted
}

@Composable
actual fun rememberContactsPermissionManager(): ContactsPermissionManager {
    return remember { IosContactsPermissionManager() }
}
