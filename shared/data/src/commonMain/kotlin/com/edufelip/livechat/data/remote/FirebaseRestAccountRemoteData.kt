package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.utils.currentEpochMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestAccountRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAccountRemoteData {
    override suspend fun fetchAccountProfile(
        userId: String,
        idToken: String,
    ): AccountProfile? =
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = "${config.documentsEndpoint}/${config.usersCollection}/$userId"
            val document =
                runCatching {
                    httpClient.get(url) {
                        header(AUTHORIZATION_HEADER, "Bearer $idToken")
                        if (config.apiKey.isNotBlank()) {
                            parameter("key", config.apiKey)
                        }
                    }.body<FirestoreDocument>()
                }.getOrNull() ?: return@withContext null

            document.toAccountProfile(userId)
        }

    override suspend fun updateDisplayName(
        userId: String,
        idToken: String,
        displayName: String,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_DISPLAY_NAME to Value(stringValue = displayName)))
    }

    override suspend fun updateStatusMessage(
        userId: String,
        idToken: String,
        statusMessage: String,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_STATUS_MESSAGE to Value(stringValue = statusMessage)))
    }

    override suspend fun updateEmail(
        userId: String,
        idToken: String,
        email: String,
    ) {
        updateFields(userId, idToken, mapOf(FIELD_EMAIL to Value(stringValue = email)))
    }

    override suspend fun deleteAccount(
        userId: String,
        idToken: String,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            markAccountDeleted(userId, idToken)
            deleteAuthUser(idToken)
        }
    }

    private suspend fun updateFields(
        userId: String,
        idToken: String,
        fields: Map<String, Value>,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val url = "${config.documentsEndpoint}/${config.usersCollection}/$userId"
            val request = UpdateDocumentRequest(fields = fields)
            httpClient.patch(url) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                fields.keys.forEach { parameter("updateMask.fieldPaths", it) }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    private suspend fun markAccountDeleted(
        userId: String,
        idToken: String,
    ) {
        val now = currentEpochMillis().toString()
        updateFields(
            userId = userId,
            idToken = idToken,
            fields =
                mapOf(
                    FIELD_IS_DELETED to Value(booleanValue = true),
                    FIELD_DELETED_AT to Value(integerValue = now),
                    FIELD_DISPLAY_NAME to Value(stringValue = DELETED_USER_NAME),
                    FIELD_STATUS_MESSAGE to Value(stringValue = ""),
                    FIELD_PHONE_NUMBER to Value(stringValue = ""),
                    FIELD_EMAIL to Value(stringValue = ""),
                    FIELD_PHOTO_URL to Value(stringValue = ""),
                ),
        )
    }

    private suspend fun deleteAuthUser(idToken: String) {
        require(config.apiKey.isNotBlank()) { "Firebase API key is required to delete accounts" }
        val url = AUTH_DELETE_ENDPOINT
        httpClient.post(url) {
            parameter("key", config.apiKey)
            contentType(ContentType.Application.Json)
            setBody(DeleteAccountRequest(idToken = idToken))
        }
    }

    private fun ensureConfigured(idToken: String) {
        require(config.isConfigured) { "Firebase projectId is missing" }
        require(idToken.isNotBlank()) { "Missing auth token" }
    }

    private fun FirestoreDocument.toAccountProfile(userId: String): AccountProfile {
        val displayName = fields[FIELD_DISPLAY_NAME]?.stringValue.orEmpty()
        return AccountProfile(
            userId = userId,
            displayName = displayName,
            statusMessage = fields[FIELD_STATUS_MESSAGE]?.stringValue,
            phoneNumber = fields[FIELD_PHONE_NUMBER]?.stringValue,
            email = fields[FIELD_EMAIL]?.stringValue,
            photoUrl = fields[FIELD_PHOTO_URL]?.stringValue,
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
    private data class DeleteAccountRequest(
        val idToken: String,
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("booleanValue") val booleanValue: Boolean? = null,
        @SerialName("integerValue") val integerValue: String? = null,
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val AUTH_DELETE_ENDPOINT = "https://identitytoolkit.googleapis.com/v1/accounts:delete"
        const val FIELD_DISPLAY_NAME = "display_name"
        const val FIELD_STATUS_MESSAGE = "status_message"
        const val FIELD_PHONE_NUMBER = "phone_num"
        const val FIELD_EMAIL = "email"
        const val FIELD_PHOTO_URL = "photo_url"
        const val FIELD_IS_DELETED = "is_deleted"
        const val FIELD_DELETED_AT = "deleted_at"
        const val DELETED_USER_NAME = "Deleted user"
    }
}
