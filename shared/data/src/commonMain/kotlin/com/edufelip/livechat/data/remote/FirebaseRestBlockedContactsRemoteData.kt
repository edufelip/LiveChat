package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IBlockedContactsRemoteData
import com.edufelip.livechat.domain.models.BlockedContact
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
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

class FirebaseRestBlockedContactsRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IBlockedContactsRemoteData {
    override suspend fun fetchBlockedContacts(
        userId: String,
        idToken: String,
    ): List<BlockedContact> =
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val response =
                runCatching {
                    httpClient.get(collectionUrl(userId)) {
                        header(AUTHORIZATION_HEADER, "Bearer $idToken")
                        if (config.apiKey.isNotBlank()) {
                            parameter("key", config.apiKey)
                        }
                    }.body<ListDocumentsResponse>()
                }.getOrNull() ?: return@withContext emptyList()

            response.documents.mapNotNull { it.toBlockedContact() }
        }

    override suspend fun blockContact(
        userId: String,
        idToken: String,
        contact: BlockedContact,
    ) {
        val fields =
            buildMap {
                put(FIELD_BLOCKED_USER_ID, Value(stringValue = contact.userId))
                contact.displayName?.let { put(FIELD_DISPLAY_NAME, Value(stringValue = it)) }
                contact.phoneNumber?.let { put(FIELD_PHONE_NUMBER, Value(stringValue = it)) }
            }
        updateFields(userId, idToken, contact.userId, fields)
    }

    override suspend fun unblockContact(
        userId: String,
        idToken: String,
        blockedUserId: String,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            httpClient.delete(documentUrl(userId, blockedUserId)) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
            }
        }
    }

    private suspend fun updateFields(
        userId: String,
        idToken: String,
        blockedUserId: String,
        fields: Map<String, Value>,
    ) {
        withContext(dispatcher) {
            ensureConfigured(idToken)
            val request = UpdateDocumentRequest(fields = fields)
            runCatching {
                httpClient.patch(documentUrl(userId, blockedUserId)) {
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
                        createDocument(userId, idToken, blockedUserId, request)
                        return@onFailure
                    }
                }
                println(
                    "FirebaseRestBlockedContactsRemoteData: update failed userId=$userId blocked=$blockedUserId error=${throwable.message}",
                )
            }
        }
    }

    private suspend fun createDocument(
        userId: String,
        idToken: String,
        blockedUserId: String,
        request: UpdateDocumentRequest,
    ) {
        runCatching {
            httpClient.post(collectionUrl(userId)) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                parameter("documentId", blockedUserId)
                contentType(ContentType.Application.Json)
                setBody(CreateDocumentRequest(fields = request.fields))
            }
        }.onFailure { throwable ->
            println(
                "FirebaseRestBlockedContactsRemoteData: create failed userId=$userId blocked=$blockedUserId error=${throwable.message}",
            )
        }
    }

    private fun collectionUrl(userId: String): String =
        "${config.documentsEndpoint}/${config.usersCollection}/$userId/$BLOCKED_CONTACTS_COLLECTION"

    private fun documentUrl(
        userId: String,
        blockedUserId: String,
    ): String = "${collectionUrl(userId)}/$blockedUserId"

    private fun ensureConfigured(idToken: String) {
        require(config.isConfigured) { "Firebase projectId is missing" }
        require(idToken.isNotBlank()) { "Missing auth token" }
    }

    private fun FirestoreDocument.toBlockedContact(): BlockedContact? {
        val fallbackId = fields[FIELD_BLOCKED_USER_ID]?.stringValue
        val idFromName = name.substringAfterLast('/').takeIf { it.isNotBlank() }
        val userId = idFromName ?: fallbackId ?: return null
        return BlockedContact(
            userId = userId,
            displayName = fields[FIELD_DISPLAY_NAME]?.stringValue,
            phoneNumber = fields[FIELD_PHONE_NUMBER]?.stringValue,
        )
    }

    @Serializable
    private data class ListDocumentsResponse(
        val documents: List<FirestoreDocument> = emptyList(),
    )

    @Serializable
    private data class FirestoreDocument(
        val name: String = "",
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
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BLOCKED_CONTACTS_COLLECTION = "blockedContacts"
        const val FIELD_BLOCKED_USER_ID = "blocked_user_id"
        const val FIELD_DISPLAY_NAME = "display_name"
        const val FIELD_PHONE_NUMBER = "phone_number"
    }
}
