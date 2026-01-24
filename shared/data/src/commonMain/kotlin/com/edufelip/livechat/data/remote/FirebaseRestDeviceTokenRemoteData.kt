package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IDeviceTokenRemoteData
import com.edufelip.livechat.domain.models.DevicePlatform
import com.edufelip.livechat.domain.models.DeviceToken
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.edufelip.livechat.domain.utils.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestDeviceTokenRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IDeviceTokenRemoteData {
    override suspend fun registerToken(
        userId: String,
        idToken: String,
        registration: DeviceTokenRegistration,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = deviceTokenDocumentUrl(userId, registration.deviceId)
            val fields =
                mapOf(
                    FIELD_FCM_TOKEN to Value(stringValue = registration.fcmToken),
                    FIELD_PLATFORM to Value(stringValue = registration.platform.name.lowercase()),
                    FIELD_LAST_UPDATED_AT to Value(integerValue = currentEpochMillis().toString()),
                    FIELD_IS_ACTIVE to Value(booleanValue = true),
                ).let { base ->
                    if (registration.appVersion != null) {
                        base + (FIELD_APP_VERSION to Value(stringValue = registration.appVersion))
                    } else {
                        base
                    }
                }

            httpClient.patch(url) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                contentType(ContentType.Application.Json)
                setBody(
                    FirestoreDocument(
                        name = url,
                        fields = fields,
                    ),
                )
            }
        }
    }

    override suspend fun unregisterToken(
        userId: String,
        idToken: String,
        deviceId: String,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = deviceTokenDocumentUrl(userId, deviceId)
            httpClient.delete(url) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
            }
        }
    }

    override suspend fun getTokens(
        userId: String,
        idToken: String,
    ): List<DeviceToken> {
        return withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = deviceTokensCollectionUrl(userId)
            val response =
                httpClient.get(url) {
                    header(AUTHORIZATION_HEADER, "Bearer $idToken")
                    if (config.apiKey.isNotBlank()) {
                        parameter("key", config.apiKey)
                    }
                }.body<ListDocumentsResponse>()

            response.documents?.mapNotNull { it.toDeviceToken() } ?: emptyList()
        }
    }

    override suspend fun cleanupInactiveTokens(
        userId: String,
        idToken: String,
    ) {
        withContext(dispatcher) {
            // Get all tokens
            val tokens = getTokens(userId, idToken)
            val now = currentEpochMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            // Delete tokens older than 30 days
            tokens
                .filter { it.lastUpdatedAt < thirtyDaysAgo }
                .forEach { token ->
                    runCatching {
                        unregisterToken(userId, idToken, token.deviceId)
                    }
                }
        }
    }

    private fun deviceTokenDocumentUrl(
        userId: String,
        deviceId: String,
    ): String = "${config.firestoreBaseUrl}/users/$userId/devices/$deviceId"

    private fun deviceTokensCollectionUrl(userId: String): String = "${config.firestoreBaseUrl}/users/$userId/devices"

    private fun ensureConfigured(idToken: String) {
        if (config.projectId.isBlank() || idToken.isBlank()) {
            error("Firebase REST API is not configured. Ensure projectId and idToken are set.")
        }
    }

    private fun FirestoreDocument.toDeviceToken(): DeviceToken? {
        val deviceId = name.substringAfterLast("/")
        val fcmToken = fields[FIELD_FCM_TOKEN]?.stringValue ?: return null
        val platformStr = fields[FIELD_PLATFORM]?.stringValue ?: return null
        val platform =
            when (platformStr.lowercase()) {
                "android" -> DevicePlatform.Android
                "ios" -> DevicePlatform.iOS
                else -> return null
            }
        val lastUpdatedAt = fields[FIELD_LAST_UPDATED_AT]?.integerValue?.toLongOrNull() ?: 0L
        val appVersion = fields[FIELD_APP_VERSION]?.stringValue
        val isActive = fields[FIELD_IS_ACTIVE]?.booleanValue ?: true

        return DeviceToken(
            deviceId = deviceId,
            fcmToken = fcmToken,
            platform = platform,
            lastUpdatedAt = lastUpdatedAt,
            appVersion = appVersion,
            isActive = isActive,
        )
    }

    @Serializable
    private data class FirestoreDocument(
        val name: String,
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class Value(
        val stringValue: String? = null,
        val integerValue: String? = null,
        val booleanValue: Boolean? = null,
    )

    @Serializable
    private data class ListDocumentsResponse(
        val documents: List<FirestoreDocument>? = null,
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val FIELD_FCM_TOKEN = "fcm_token"
        const val FIELD_PLATFORM = "platform"
        const val FIELD_LAST_UPDATED_AT = "last_updated_at"
        const val FIELD_APP_VERSION = "app_version"
        const val FIELD_IS_ACTIVE = "is_active"
    }
}
