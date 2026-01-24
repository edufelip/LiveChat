package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IDeviceTokenRemoteData
import com.edufelip.livechat.domain.models.DeviceToken
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IDeviceTokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceTokenRepository(
    private val remoteData: IDeviceTokenRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IDeviceTokenRepository {
    override suspend fun registerDeviceToken(registration: DeviceTokenRegistration) {
        withContext(dispatcher) {
            val session =
                sessionProvider.requireSession()
                    ?: error("User must be authenticated to register device tokens")
            remoteData.registerToken(
                userId = session.userId,
                idToken = session.idToken,
                registration = registration,
            )
        }
    }

    override suspend fun unregisterDeviceToken(deviceId: String) {
        withContext(dispatcher) {
            val session =
                sessionProvider.requireSession()
                    ?: error("User must be authenticated to unregister device tokens")
            remoteData.unregisterToken(
                userId = session.userId,
                idToken = session.idToken,
                deviceId = deviceId,
            )
        }
    }

    override suspend fun getDeviceTokens(): List<DeviceToken> {
        return withContext(dispatcher) {
            val session =
                sessionProvider.requireSession()
                    ?: error("User must be authenticated to get device tokens")
            remoteData.getTokens(
                userId = session.userId,
                idToken = session.idToken,
            )
        }
    }

    override suspend fun cleanupInactiveTokens() {
        withContext(dispatcher) {
            val session = sessionProvider.requireSession() ?: return@withContext
            remoteData.cleanupInactiveTokens(
                userId = session.userId,
                idToken = session.idToken,
            )
        }
    }
}
