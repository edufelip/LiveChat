package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.DeviceToken
import com.edufelip.livechat.domain.models.DeviceTokenRegistration

interface IDeviceTokenRepository {
    /**
     * Registers or updates an FCM device token for the current user.
     * This ensures the backend can send push notifications to this device.
     */
    suspend fun registerDeviceToken(registration: DeviceTokenRegistration)

    /**
     * Removes a device token (e.g., on logout or when token is invalidated).
     */
    suspend fun unregisterDeviceToken(deviceId: String)

    /**
     * Gets all registered device tokens for the current user.
     */
    suspend fun getDeviceTokens(): List<DeviceToken>

    /**
     * Marks old tokens as inactive (e.g., after a certain period).
     */
    suspend fun cleanupInactiveTokens()
}
