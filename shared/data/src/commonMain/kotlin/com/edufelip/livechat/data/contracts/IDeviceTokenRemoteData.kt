package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.DeviceToken
import com.edufelip.livechat.domain.models.DeviceTokenRegistration

interface IDeviceTokenRemoteData {
    suspend fun registerToken(
        userId: String,
        idToken: String,
        registration: DeviceTokenRegistration,
    )

    suspend fun unregisterToken(
        userId: String,
        idToken: String,
        deviceId: String,
    )

    suspend fun getTokens(
        userId: String,
        idToken: String,
    ): List<DeviceToken>

    suspend fun cleanupInactiveTokens(
        userId: String,
        idToken: String,
    )
}
