package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.INotificationSettingsRemoteData
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.domain.models.QuietHours
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestNotificationSettingsRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : INotificationSettingsRemoteData {
    override suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): NotificationSettings? =
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val document =
                runCatching {
                    httpClient.get(documentUrl(userId)) {
                        header(AUTHORIZATION_HEADER, "Bearer $idToken")
                        if (config.apiKey.isNotBlank()) {
                            parameter("key", config.apiKey)
                        }
                    }.body<FirestoreDocument>()
                }.getOrNull() ?: return@withContext null

            document.toSettings()
        }

    override suspend fun updatePushEnabled(
        userId: String,
        idToken: String,
        enabled: Boolean,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_PUSH_ENABLED to Value(booleanValue = enabled)))
    }

    override suspend fun updateSound(
        userId: String,
        idToken: String,
        sound: String,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_SOUND to Value(stringValue = sound)))
    }

    override suspend fun updateQuietHoursEnabled(
        userId: String,
        idToken: String,
        enabled: Boolean,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_QUIET_ENABLED to Value(booleanValue = enabled)))
    }

    override suspend fun updateQuietHoursWindow(
        userId: String,
        idToken: String,
        from: String,
        to: String,
    ) {
        updateFields(
            userId,
            idToken,
            mapOf(
                FIELD_QUIET_FROM to Value(stringValue = from),
                FIELD_QUIET_TO to Value(stringValue = to),
            ),
        )
    }

    override suspend fun updateInAppVibration(
        userId: String,
        idToken: String,
        enabled: Boolean,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_IN_APP_VIBRATION to Value(booleanValue = enabled)))
    }

    override suspend fun updateShowMessagePreview(
        userId: String,
        idToken: String,
        enabled: Boolean,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_SHOW_PREVIEW to Value(booleanValue = enabled)))
    }

    override suspend fun resetSettings(
        userId: String,
        idToken: String,
    ) {
        val defaults = NotificationSettings()
        updateFields(
            userId,
            idToken,
            mapOf(
                FIELD_PUSH_ENABLED to Value(booleanValue = defaults.pushEnabled),
                FIELD_SOUND to Value(stringValue = defaults.sound),
                FIELD_QUIET_ENABLED to Value(booleanValue = defaults.quietHoursEnabled),
                FIELD_QUIET_FROM to Value(stringValue = defaults.quietHours.from),
                FIELD_QUIET_TO to Value(stringValue = defaults.quietHours.to),
                FIELD_IN_APP_VIBRATION to Value(booleanValue = defaults.inAppVibration),
                FIELD_SHOW_PREVIEW to Value(booleanValue = defaults.showMessagePreview),
            ),
        )
    }

    private suspend fun updateFields(
        userId: String,
        idToken: String,
        fields: Map<String, Value>,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = documentUrl(userId)
            val request = UpdateDocumentRequest(fields = fields)

            try {
                httpClient.patch(url) {
                    header(AUTHORIZATION_HEADER, "Bearer $idToken")
                    if (config.apiKey.isNotBlank()) {
                        parameter("key", config.apiKey)
                    }
                    fields.keys.forEach { parameter("updateMask.fieldPaths", it) }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.NotFound) {
                    // Document doesn't exist, create it with defaults + changes
                    val defaults = NotificationSettings()
                    val initialFields =
                        mapOf(
                            FIELD_PUSH_ENABLED to Value(booleanValue = defaults.pushEnabled),
                            FIELD_SOUND to Value(stringValue = defaults.sound),
                            FIELD_QUIET_ENABLED to Value(booleanValue = defaults.quietHoursEnabled),
                            FIELD_QUIET_FROM to Value(stringValue = defaults.quietHours.from),
                            FIELD_QUIET_TO to Value(stringValue = defaults.quietHours.to),
                            FIELD_IN_APP_VIBRATION to Value(booleanValue = defaults.inAppVibration),
                            FIELD_SHOW_PREVIEW to Value(booleanValue = defaults.showMessagePreview),
                        ) + fields

                    httpClient.post("${config.documentsEndpoint}/${config.usersCollection}/$userId/$SETTINGS_COLLECTION") {
                        header(AUTHORIZATION_HEADER, "Bearer $idToken")
                        if (config.apiKey.isNotBlank()) {
                            parameter("key", config.apiKey)
                        }
                        parameter("documentId", NOTIFICATIONS_DOC)
                        contentType(ContentType.Application.Json)
                        setBody(UpdateDocumentRequest(fields = initialFields))
                    }
                } else {
                    throw e
                }
            }
        }
    }

    private fun documentUrl(userId: String): String =
        "${config.documentsEndpoint}/${config.usersCollection}/$userId/$SETTINGS_COLLECTION/$NOTIFICATIONS_DOC"

    private fun ensureConfigured(idToken: String) {
        require(config.isConfigured) { "Firebase projectId is missing" }
        require(idToken.isNotBlank()) { "Missing auth token" }
    }

    private fun FirestoreDocument.toSettings(): NotificationSettings {
        val defaults = NotificationSettings()
        val quietHours =
            QuietHours(
                from = fields[FIELD_QUIET_FROM]?.stringValue ?: defaults.quietHours.from,
                to = fields[FIELD_QUIET_TO]?.stringValue ?: defaults.quietHours.to,
            )
        val rawSound = fields[FIELD_SOUND]?.stringValue ?: defaults.sound
        return defaults.copy(
            pushEnabled = fields[FIELD_PUSH_ENABLED]?.booleanValue ?: defaults.pushEnabled,
            sound = NotificationSound.normalizeId(rawSound),
            quietHoursEnabled = fields[FIELD_QUIET_ENABLED]?.booleanValue ?: defaults.quietHoursEnabled,
            quietHours = quietHours,
            inAppVibration = fields[FIELD_IN_APP_VIBRATION]?.booleanValue ?: defaults.inAppVibration,
            showMessagePreview = fields[FIELD_SHOW_PREVIEW]?.booleanValue ?: defaults.showMessagePreview,
        )
    }

    @Serializable
    private data class FirestoreDocument(
        val fields: Map<String, Value> = emptyMap(),
    )

    @Serializable
    private data class UpdateDocumentRequest(
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("booleanValue") val booleanValue: Boolean? = null,
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val SETTINGS_COLLECTION = "settings"
        const val NOTIFICATIONS_DOC = "notifications"
        const val FIELD_PUSH_ENABLED = "push_enabled"
        const val FIELD_SOUND = "sound"
        const val FIELD_QUIET_ENABLED = "quiet_hours_enabled"
        const val FIELD_QUIET_FROM = "quiet_from"
        const val FIELD_QUIET_TO = "quiet_to"
        const val FIELD_IN_APP_VIBRATION = "in_app_vibration"
        const val FIELD_SHOW_PREVIEW = "show_preview"
    }
}
