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
            println(
                "[FCM] DeviceTokenRepository.registerDeviceToken: starting registration for " +
                    "deviceId=${registration.deviceId}, platform=${registration.platform}",
            )
            val session = requireSession()
            println("[FCM] DeviceTokenRepository.registerDeviceToken: got session for userId=${session.userId}")
            remoteData.registerToken(
                userId = session.userId,
                idToken = session.idToken,
                registration = registration,
            )
            println(
                "[FCM] DeviceTokenRepository.registerDeviceToken: successfully registered token for " +
                    "deviceId=${registration.deviceId}",
            )
        }
    }

    override suspend fun unregisterDeviceToken(deviceId: String) {
        withContext(dispatcher) {
            println("[FCM] DeviceTokenRepository.unregisterDeviceToken: unregistering deviceId=$deviceId")
            val session = requireSession()
            remoteData.unregisterToken(
                userId = session.userId,
                idToken = session.idToken,
                deviceId = deviceId,
            )
            println(
                "[FCM] DeviceTokenRepository.unregisterDeviceToken: successfully unregistered deviceId=$deviceId",
            )
        }
    }

    override suspend fun getDeviceTokens(): List<DeviceToken> {
        return withContext(dispatcher) {
            println("[FCM] DeviceTokenRepository.getDeviceTokens: fetching device tokens")
            val session = requireSession()
            val tokens =
                remoteData.getTokens(
                    userId = session.userId,
                    idToken = session.idToken,
                )
            println("[FCM] DeviceTokenRepository.getDeviceTokens: fetched ${tokens.size} tokens")
            tokens
        }
    }

    override suspend fun cleanupInactiveTokens() {
        withContext(dispatcher) {
            println("[FCM] DeviceTokenRepository.cleanupInactiveTokens: starting cleanup")
            val session =
                sessionProvider.refreshSession(forceRefresh = false) ?: run {
                    println("[FCM] DeviceTokenRepository.cleanupInactiveTokens: no session, skipping cleanup")
                    return@withContext
                }
            remoteData.cleanupInactiveTokens(
                userId = session.userId,
                idToken = session.idToken,
            )
            println("[FCM] DeviceTokenRepository.cleanupInactiveTokens: cleanup completed")
        }
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("User must be authenticated to register device tokens")
}
