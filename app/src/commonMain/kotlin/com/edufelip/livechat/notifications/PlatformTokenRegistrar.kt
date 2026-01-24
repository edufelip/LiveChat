package com.edufelip.livechat.notifications

/**
 * Platform-specific helper to register FCM/APNs tokens on app startup.
 */
expect object PlatformTokenRegistrar {
    fun registerCurrentToken()
}
