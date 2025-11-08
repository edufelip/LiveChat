package com.edufelip.livechat.composeapp.contacts

import androidx.compose.runtime.Composable

/**
 * Cross-platform contract for requesting access to the device contacts.
 */
interface ContactsPermissionManager {
    suspend fun ensurePermission(): PermissionState

    fun status(): PermissionState
}

sealed interface PermissionState {
    object Granted : PermissionState

    object Denied : PermissionState
}

@Composable
expect fun rememberContactsPermissionManager(): ContactsPermissionManager
