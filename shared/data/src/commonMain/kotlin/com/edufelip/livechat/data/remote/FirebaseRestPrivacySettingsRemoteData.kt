package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IPrivacySettingsRemoteData
import com.edufelip.livechat.domain.models.InvitePreference
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestPrivacySettingsRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IPrivacySettingsRemoteData {
    override suspend fun fetchSettings(
        userId: String,
        idToken: String,
    ): PrivacySettings? =
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

    override suspend fun updateInvitePreference(
        userId: String,
        idToken: String,
        preference: InvitePreference,
    ) {
        updateFields(
            userId,
            idToken,
            mapOf(FIELD_INVITE_PREFERENCE to Value(stringValue = preference.toRemoteValue())),
        )
    }

    override suspend fun updateLastSeenAudience(
        userId: String,
        idToken: String,
        audience: LastSeenAudience,
    ) {
        updateFields(
            userId,
            idToken,
            mapOf(FIELD_LAST_SEEN to Value(stringValue = audience.toRemoteValue())),
        )
    }

    override suspend fun updateReadReceipts(
        userId: String,
        idToken: String,
        enabled: Boolean,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_READ_RECEIPTS to Value(booleanValue = enabled)))
    }

    override suspend fun resetSettings(
        userId: String,
        idToken: String,
    ) {
        val defaults = PrivacySettings()
        updateFields(
            userId,
            idToken,
            mapOf(
                FIELD_INVITE_PREFERENCE to Value(stringValue = defaults.invitePreference.toRemoteValue()),
                FIELD_LAST_SEEN to Value(stringValue = defaults.lastSeenAudience.toRemoteValue()),
                FIELD_READ_RECEIPTS to Value(booleanValue = defaults.readReceiptsEnabled),
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
            val request = UpdateDocumentRequest(fields = fields)
            runCatching {
                httpClient.patch(documentUrl(userId)) {
                    header(AUTHORIZATION_HEADER, "Bearer $idToken")
                    if (config.apiKey.isNotBlank()) {
                        parameter("key", config.apiKey)
                    }
                    fields.keys.forEach { parameter("updateMask.fieldPaths", it) }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }.onFailure { throwable ->
                if (throwable is ClientRequestException) {
                    val responseBody = throwable.response.bodyAsText()
                    if (responseBody.contains("NOT_FOUND", ignoreCase = true)) {
                        createDocument(userId, idToken, request)
                        return@onFailure
                    }
                }
                println("FirebaseRestPrivacySettingsRemoteData: update failed userId=$userId error=${throwable.message}")
            }
        }
    }

    private suspend fun createDocument(
        userId: String,
        idToken: String,
        request: UpdateDocumentRequest,
    ) {
        runCatching {
            httpClient.post(collectionUrl(userId)) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                parameter("documentId", PRIVACY_DOC)
                contentType(ContentType.Application.Json)
                setBody(CreateDocumentRequest(fields = request.fields))
            }
        }.onFailure { throwable ->
            println("FirebaseRestPrivacySettingsRemoteData: create failed userId=$userId error=${throwable.message}")
        }
    }

    private fun collectionUrl(userId: String): String = "${config.documentsEndpoint}/${config.usersCollection}/$userId/$SETTINGS_COLLECTION"

    private fun documentUrl(userId: String): String =
        "${config.documentsEndpoint}/${config.usersCollection}/$userId/$SETTINGS_COLLECTION/$PRIVACY_DOC"

    private fun ensureConfigured(idToken: String) {
        require(config.isConfigured) { "Firebase projectId is missing" }
        require(idToken.isNotBlank()) { "Missing auth token" }
    }

    private fun FirestoreDocument.toSettings(): PrivacySettings {
        val defaults = PrivacySettings()
        val invitePreference = invitePreferenceFrom(fields[FIELD_INVITE_PREFERENCE]?.stringValue)
        val lastSeen = lastSeenFrom(fields[FIELD_LAST_SEEN]?.stringValue)
        return defaults.copy(
            invitePreference = invitePreference,
            lastSeenAudience = lastSeen,
            readReceiptsEnabled = fields[FIELD_READ_RECEIPTS]?.booleanValue ?: defaults.readReceiptsEnabled,
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
    private data class CreateDocumentRequest(
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("booleanValue") val booleanValue: Boolean? = null,
    )

    private fun InvitePreference.toRemoteValue(): String =
        when (this) {
            InvitePreference.Everyone -> VALUE_EVERYONE
            InvitePreference.Contacts -> VALUE_CONTACTS
            InvitePreference.Nobody -> VALUE_NOBODY
        }

    private fun LastSeenAudience.toRemoteValue(): String =
        when (this) {
            LastSeenAudience.Everyone -> VALUE_EVERYONE
            LastSeenAudience.Contacts -> VALUE_CONTACTS
            LastSeenAudience.Nobody -> VALUE_NOBODY
        }

    private fun invitePreferenceFrom(value: String?): InvitePreference =
        when (value?.lowercase()) {
            VALUE_CONTACTS -> InvitePreference.Contacts
            VALUE_NOBODY -> InvitePreference.Nobody
            else -> InvitePreference.Everyone
        }

    private fun lastSeenFrom(value: String?): LastSeenAudience =
        when (value?.lowercase()) {
            VALUE_CONTACTS -> LastSeenAudience.Contacts
            VALUE_NOBODY -> LastSeenAudience.Nobody
            else -> LastSeenAudience.Everyone
        }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val SETTINGS_COLLECTION = "settings"
        const val PRIVACY_DOC = "privacy"
        const val FIELD_INVITE_PREFERENCE = "invite_preference"
        const val FIELD_LAST_SEEN = "last_seen_audience"
        const val FIELD_READ_RECEIPTS = "read_receipts"
        const val VALUE_EVERYONE = "everyone"
        const val VALUE_CONTACTS = "contacts"
        const val VALUE_NOBODY = "nobody"
    }
}
