package com.edufelip.livechat.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Contacts.CNAuthorizationStatusAuthorized
import platform.Contacts.CNAuthorizationStatusDenied
import platform.Contacts.CNAuthorizationStatusNotDetermined
import platform.Contacts.CNAuthorizationStatusRestricted
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults

private class IosContactsPermissionManager : ContactsPermissionManager {
    private val store = CNContactStore()
    private var currentState: PermissionState =
        when {
            isContactsUiTestDenied() -> PermissionState.Denied
            isContactsUiTestFlowEnabled() -> PermissionState.Denied
            else -> authorizationStatusToState()
        }

    override suspend fun ensurePermission(): PermissionState {
        if (isContactsUiTestDenied()) {
            currentState = PermissionState.Denied
            return currentState
        }
        if (isContactsUiTestFlowEnabled()) {
            currentState = PermissionState.Denied
            return currentState
        }
        currentState =
            when (CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts)) {
                CNAuthorizationStatusAuthorized -> PermissionState.Granted
                CNAuthorizationStatusDenied, CNAuthorizationStatusRestricted -> PermissionState.Denied
                CNAuthorizationStatusNotDetermined -> {
                    if (requestAccess()) PermissionState.Granted else PermissionState.Denied
                }
                else -> PermissionState.Denied
            }
        return currentState
    }

    override fun status(): PermissionState = currentState

    private fun authorizationStatusToState(): PermissionState {
        return when (CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts)) {
            CNAuthorizationStatusAuthorized -> PermissionState.Granted
            CNAuthorizationStatusDenied, CNAuthorizationStatusRestricted -> PermissionState.Denied
            CNAuthorizationStatusNotDetermined -> PermissionState.Denied
            else -> PermissionState.Denied
        }
    }

    private suspend fun requestAccess(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            store.requestAccessForEntityType(CNEntityType.CNEntityTypeContacts) { granted, _ ->
                if (continuation.isActive) {
                    continuation.resume(granted)
                }
            }
        }
    }
}

@Composable
actual fun rememberContactsPermissionManager(): ContactsPermissionManager {
    return remember { IosContactsPermissionManager() }
}

private fun isContactsUiTestFlowEnabled(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    val envValue = environment["UITEST_CONTACTS_FLOW"]?.toString()
    val defaultsValue = NSUserDefaults.standardUserDefaults.stringForKey("UITEST_CONTACTS_FLOW")
    return envValue == "1" || envValue == "true" || defaultsValue == "1" || defaultsValue == "true"
}

private fun isContactsUiTestDenied(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    val envValue = environment["UITEST_CONTACTS_DENY"]?.toString()
    val defaultsValue = NSUserDefaults.standardUserDefaults.stringForKey("UITEST_CONTACTS_DENY")
    return envValue == "1" || envValue == "true" || defaultsValue == "1" || defaultsValue == "true"
}
