package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IPresenceRemoteData
import com.edufelip.livechat.domain.models.PresenceState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
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

class FirebaseRestPresenceRemoteData(
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IPresenceRemoteData {
    override suspend fun updatePresence(
        userId: String,
        idToken: String,
        isOnline: Boolean,
        lastActiveAt: Long,
    ) {
        withContext(dispatcher) {
            if (!config.isConfigured || idToken.isBlank() || userId.isBlank()) return@withContext
            val url = "${config.documentsEndpoint}/${config.presenceCollection}/$userId"
            val request =
                UpdateDocumentRequest(
                    fields =
                        mapOf(
                            FIELD_STATE to Value(stringValue = if (isOnline) STATE_ONLINE else STATE_OFFLINE),
                            FIELD_LAST_ACTIVE_AT to Value(integerValue = lastActiveAt.toString()),
                        ),
                )

            runCatching {
                httpClient.patch(url) {
                    header(AUTHORIZATION_HEADER, "Bearer $idToken")
                    if (config.apiKey.isNotBlank()) {
                        parameter("key", config.apiKey)
                    }
                    request.fields.keys.forEach { field ->
                        parameter("updateMask.fieldPaths", field)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }.onFailure { throwable ->
                if (throwable is ClientRequestException) {
                    val responseBody = throwable.response.bodyAsText()
                    if (responseBody.contains("NOT_FOUND", ignoreCase = true)) {
                        createPresence(userId, idToken, request)
                        return@onFailure
                    }
                }
                println("FirebaseRestPresenceRemoteData: updatePresence failed userId=$userId error=${throwable.message}")
            }
        }
    }

    override suspend fun fetchPresence(
        userIds: List<String>,
        idToken: String,
    ): Map<String, PresenceState> =
        withContext(dispatcher) {
            if (!config.isConfigured || idToken.isBlank() || userIds.isEmpty()) {
                return@withContext emptyMap()
            }
            val results = mutableMapOf<String, PresenceState>()
            val chunks = userIds.distinct().chunked(QUERY_CHUNK_SIZE)
            chunks.forEach { chunk ->
                val response = queryPresenceChunk(chunk, idToken)
                response.forEach { document ->
                    val userId = document.name.substringAfterLast('/')
                    val state = document.fields[FIELD_STATE]?.stringValue.orEmpty()
                    val lastActiveAt = document.fields[FIELD_LAST_ACTIVE_AT]?.integerValue?.toLongOrNull() ?: 0L
                    val isOnline = state == STATE_ONLINE
                    results[userId] = PresenceState(userId = userId, isOnline = isOnline, lastActiveAt = lastActiveAt)
                }
            }
            results
        }

    private suspend fun queryPresenceChunk(
        userIds: List<String>,
        idToken: String,
    ): List<FirestoreDocument> {
        val documentRefs =
            userIds.map { userId ->
                "projects/${config.projectId}/databases/(default)/documents/${config.presenceCollection}/$userId"
            }
        val request =
            RunQueryRequest(
                structuredQuery =
                    StructuredQuery(
                        from = listOf(CollectionSelector(collectionId = config.presenceCollection)),
                        where =
                            FieldFilterWrapper(
                                fieldFilter =
                                    FieldFilter(
                                        field = FieldReference(fieldPath = "__name__"),
                                        op = "IN",
                                        value =
                                            Value(
                                                arrayValue =
                                                    ArrayValue(
                                                        values = documentRefs.map { ref -> Value(referenceValue = ref) },
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val response =
            runCatching {
                httpClient
                    .post(config.queryEndpoint) {
                        header(AUTHORIZATION_HEADER, "Bearer $idToken")
                        if (config.apiKey.isNotBlank()) {
                            parameter("key", config.apiKey)
                        }
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body<List<RunQueryResponse>>()
            }.getOrElse { throwable ->
                println("FirebaseRestPresenceRemoteData: query failed error=${throwable.message}")
                emptyList()
            }
        return response.mapNotNull { it.document }
    }

    private suspend fun createPresence(
        userId: String,
        idToken: String,
        request: UpdateDocumentRequest,
    ) {
        val url = "${config.documentsEndpoint}/${config.presenceCollection}"
        runCatching {
            httpClient.post(url) {
                header(AUTHORIZATION_HEADER, "Bearer $idToken")
                if (config.apiKey.isNotBlank()) {
                    parameter("key", config.apiKey)
                }
                parameter("documentId", userId)
                contentType(ContentType.Application.Json)
                setBody(CreateDocumentRequest(fields = request.fields))
            }
        }.onFailure { throwable ->
            println("FirebaseRestPresenceRemoteData: createPresence failed userId=$userId error=${throwable.message}")
        }
    }

    @Serializable
    private data class RunQueryRequest(
        val structuredQuery: StructuredQuery,
    )

    @Serializable
    private data class StructuredQuery(
        val from: List<CollectionSelector>,
        val where: FieldFilterWrapper,
    )

    @Serializable
    private data class CollectionSelector(
        val collectionId: String,
    )

    @Serializable
    private data class FieldFilterWrapper(
        val fieldFilter: FieldFilter,
    )

    @Serializable
    private data class FieldFilter(
        val field: FieldReference,
        val op: String,
        val value: Value,
    )

    @Serializable
    private data class FieldReference(
        val fieldPath: String,
    )

    @Serializable
    private data class ArrayValue(
        val values: List<Value> = emptyList(),
    )

    @Serializable
    private data class RunQueryResponse(
        val document: FirestoreDocument? = null,
    )

    @Serializable
    private data class FirestoreDocument(
        val name: String,
        val fields: Map<String, Value> = emptyMap(),
    )

    @Serializable
    private data class CreateDocumentRequest(
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class UpdateDocumentRequest(
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("integerValue") val integerValue: String? = null,
        @SerialName("referenceValue") val referenceValue: String? = null,
        @SerialName("arrayValue") val arrayValue: ArrayValue? = null,
    )

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val STATE_ONLINE = "online"
        const val STATE_OFFLINE = "offline"
        const val FIELD_STATE = "state"
        const val FIELD_LAST_ACTIVE_AT = "last_active_at"
        const val QUERY_CHUNK_SIZE = 10
    }
}
