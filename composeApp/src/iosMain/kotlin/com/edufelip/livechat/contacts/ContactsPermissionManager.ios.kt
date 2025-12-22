package com.edufelip.livechat.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults

private class IosContactsPermissionManager : ContactsPermissionManager {
    private var currentState: PermissionState =
        when {
            isContactsUiTestDenied() -> PermissionState.Denied
            isContactsUiTestFlowEnabled() -> PermissionState.Denied
            else -> PermissionState.Granted
        }

    override suspend fun ensurePermission(): PermissionState {
        if (currentState is PermissionState.Granted || isContactsUiTestDenied()) {
            return currentState
        }
        currentState = PermissionState.Granted
        return currentState
    }

    override fun status(): PermissionState = currentState
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
