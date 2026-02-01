package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IAppearanceSettingsRemoteData
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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

class FirebaseRestAppearanceSettingsRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAppearanceSettingsRemoteData {
    override suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): AppearanceSettings? =
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

    override suspend fun updateThemeMode(
        userId: String,
        idToken: String,
        mode: ThemeMode,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_THEME_MODE to Value(stringValue = mode.toRemoteValue())))
    }

    override suspend fun updateTextScale(
        userId: String,
        idToken: String,
        scale: Float,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_TEXT_SCALE to Value(doubleValue = scale.toDouble())))
    }

    override suspend fun resetSettings(
        userId: String,
        idToken: String,
    ) {
        val defaults = AppearanceSettings()
        updateFields(
            userId,
            idToken,
            mapOf(
                FIELD_THEME_MODE to Value(stringValue = defaults.themeMode.toRemoteValue()),
                FIELD_TEXT_SCALE to Value(doubleValue = defaults.textScale.toDouble()),
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
            httpClient.patch(documentUrl(userId)) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                fields.keys.forEach { parameter("updateMask.fieldPaths", it) }
                contentType(ContentType.Application.Json)
                setBody(UpdateDocumentRequest(fields = fields))
            }
        }
    }

    private fun documentUrl(userId: String): String =
        "${config.documentsEndpoint}/${config.usersCollection}/$userId/$SETTINGS_COLLECTION/$APPEARANCE_DOC"

    private fun ensureConfigured(idToken: String) {
        require(config.isConfigured) { "Firebase projectId is missing" }
        require(idToken.isNotBlank()) { "Missing auth token" }
    }

    private fun FirestoreDocument.toSettings(): AppearanceSettings {
        val defaults = AppearanceSettings()
        val themeMode = themeModeFrom(fields[FIELD_THEME_MODE]?.stringValue)
        val textScaleValue = fields[FIELD_TEXT_SCALE]?.doubleValue?.toFloat() ?: defaults.textScale
        val textScale =
            textScaleValue.coerceIn(AppearanceSettings.MIN_TEXT_SCALE, AppearanceSettings.MAX_TEXT_SCALE)
        return defaults.copy(
            themeMode = themeMode,
            textScale = textScale,
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
        @SerialName("doubleValue") val doubleValue: Double? = null,
    )

    private fun ThemeMode.toRemoteValue(): String =
        when (this) {
            ThemeMode.System -> VALUE_SYSTEM
            ThemeMode.Light -> VALUE_LIGHT
            ThemeMode.Dark -> VALUE_DARK
        }

    private fun themeModeFrom(value: String?): ThemeMode =
        when (value?.lowercase()) {
            VALUE_LIGHT -> ThemeMode.Light
            VALUE_DARK -> ThemeMode.Dark
            else -> ThemeMode.System
        }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val SETTINGS_COLLECTION = "settings"
        const val APPEARANCE_DOC = "appearance"
        const val FIELD_THEME_MODE = "theme_mode"
        const val FIELD_TEXT_SCALE = "text_scale"
        const val VALUE_SYSTEM = "system"
        const val VALUE_LIGHT = "light"
        const val VALUE_DARK = "dark"
    }
}
