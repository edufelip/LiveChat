package com.project.livechat.data.remote

import com.project.livechat.data.contracts.IMessagesRemoteData
import com.project.livechat.domain.models.Message
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.models.MessageStatus
import com.project.livechat.domain.providers.UserSessionProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEFAULT_POLLING_LIMIT = 50

class FirebaseMessagesRemoteData(
    private val httpClient: HttpClient,
    private val config: FirebaseRestConfig,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : IMessagesRemoteData {

    override fun observeConversation(conversationId: String, sinceEpochMillis: Long?): Flow<List<Message>> =
        channelFlow {
            if (!config.isConfigured) {
                close()
                return@channelFlow
            }

            val accessToken = sessionProvider.refreshSession(false)?.idToken
            val websocketEndpoint = config.websocketEndpoint.takeIf { it.isNotBlank() }

            if (websocketEndpoint != null) {
                observeViaWebSocket(websocketEndpoint, conversationId, accessToken)
            } else {
                observeViaPolling(conversationId, sinceEpochMillis, accessToken)
            }
        }.flowOn(dispatcher)

    override suspend fun sendMessage(draft: MessageDraft): Message {
        if (!config.isConfigured) error("Firebase projectId is missing – cannot send message")

        return withContext(dispatcher) {
            val request = FirestoreCreateDocumentRequest(
                fields = mapOf(
                    FIELD_CONVERSATION_ID to FirestoreValue(stringValue = draft.conversationId),
                    FIELD_SENDER_ID to FirestoreValue(stringValue = draft.senderId),
                    FIELD_BODY to FirestoreValue(stringValue = draft.body),
                    FIELD_CREATED_AT to FirestoreValue(integerValue = draft.createdAt.toString()),
                    FIELD_STATUS to FirestoreValue(stringValue = MessageStatus.SENT.name),
                    FIELD_LOCAL_TEMP_ID to FirestoreValue(stringValue = draft.localId)
                )
            )

            val response: HttpResponse = httpClient.post("${config.documentsEndpoint}/${config.messagesCollection}") {
                parameter("key", config.apiKey)
                sessionProvider.refreshSession(false)?.idToken?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val bodyText = response.bodyAsText()
            val document = json.decodeFromString(FirestoreDocument.serializer(), bodyText)
            document.toDomainMessage()
        }
    }

    override suspend fun pullHistorical(conversationId: String, sinceEpochMillis: Long?): List<Message> {
        val accessToken = sessionProvider.refreshSession(false)?.idToken
        return fetchMessages(conversationId, sinceEpochMillis, accessToken)
    }

    private suspend fun ProducerScope<List<Message>>.observeViaPolling(
        conversationId: String,
        sinceEpochMillis: Long?,
        accessToken: String?
    ) {
        var latestTimestamp = sinceEpochMillis
        while (currentCoroutineContext().isActive && !isClosedForSend) {
            val messages = fetchMessages(conversationId, latestTimestamp, accessToken)
            if (messages.isNotEmpty()) {
                latestTimestamp = messages.maxOf { it.createdAt }
                trySend(messages)
            }
            delay(config.pollingIntervalMs)
        }
    }

    private suspend fun ProducerScope<List<Message>>.observeViaWebSocket(
        websocketEndpoint: String,
        conversationId: String,
        accessToken: String?
    ) {
        var session: WebSocketSession? = null
        try {
            session = httpClient.webSocketSession(urlString = "$websocketEndpoint/$conversationId") {
                accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }

            accessToken?.let {
                val payload = json.encodeToString(WebSocketHandshake.serializer(), WebSocketHandshake(token = it, conversationId = conversationId))
                session.send(payload)
            }

            for (frame in session.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val payload = json.decodeFromString(FirestoreDocument.serializer(), frame.readText())
                        trySend(listOf(payload.toDomainMessage()))
                    }
                    is Frame.Close -> {
                        close()
                        break
                    }
                    else -> Unit
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // Socket closed by server; let the flow complete gracefully.
        } finally {
            session?.close()
        }
    }

    private suspend fun fetchMessages(
        conversationId: String,
        sinceEpochMillis: Long?,
        accessToken: String?
    ): List<Message> = withContext(dispatcher) {
        if (!config.isConfigured) return@withContext emptyList()

        val structuredQuery = StructuredQuery(
            from = listOf(CollectionSelector(collectionId = config.messagesCollection)),
            where = buildWhere(conversationId, sinceEpochMillis),
            orderBy = listOf(Order(field = FieldReference(fieldPath = FIELD_CREATED_AT))),
            limit = DEFAULT_POLLING_LIMIT
        )

        val request = RunQueryRequest(structuredQuery)

        val responses: List<RunQueryResponse> = httpClient.post(config.queryEndpoint) {
            parameter("key", config.apiKey)
            accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        responses.mapNotNull { response ->
            response.document?.toDomainMessage()
        }
    }

    private fun buildWhere(conversationId: String, sinceEpochMillis: Long?): Where {
        val conversationFilter = FieldFilter(
            field = FieldReference(fieldPath = FIELD_CONVERSATION_ID),
            op = "EQUAL",
            value = FirestoreValue(stringValue = conversationId)
        )

        val createdAtFilter = sinceEpochMillis?.let {
            FieldFilter(
                field = FieldReference(fieldPath = FIELD_CREATED_AT),
                op = "GREATER_THAN",
                value = FirestoreValue(integerValue = it.toString())
            )
        }

        return if (createdAtFilter != null) {
            Where(
                compositeFilter = CompositeFilter(
                    op = "AND",
                    filters = listOf(
                        Filter(fieldFilter = conversationFilter),
                        Filter(fieldFilter = createdAtFilter)
                    )
                )
            )
        } else {
            Where(fieldFilter = conversationFilter)
        }
    }

    private fun FirestoreDocument.toDomainMessage(): Message {
        val fields = fields ?: emptyMap()
        val status = fields[FIELD_STATUS]?.stringValue?.let {
            runCatching { MessageStatus.valueOf(it) }.getOrDefault(MessageStatus.SENT)
        } ?: MessageStatus.SENT

        val idFromName = name?.substringAfterLast('/') ?: fields[FIELD_LOCAL_TEMP_ID]?.stringValue ?: ""
        return Message(
            id = idFromName,
            conversationId = fields[FIELD_CONVERSATION_ID]?.stringValue.orEmpty(),
            senderId = fields[FIELD_SENDER_ID]?.stringValue.orEmpty(),
            body = fields[FIELD_BODY]?.stringValue.orEmpty(),
            createdAt = fields[FIELD_CREATED_AT]?.asLong() ?: 0L,
            status = status,
            localTempId = fields[FIELD_LOCAL_TEMP_ID]?.stringValue
        )
    }

    private fun FirestoreValue.asLong(): Long? = integerValue?.toLongOrNull() ?: timestampValue?.let { timestamp ->
        runCatching { Instant.parse(timestamp).toEpochMilliseconds() }.getOrNull()
    }

    @Serializable
    private data class WebSocketHandshake(
        @SerialName("token") val token: String,
        @SerialName("conversationId") val conversationId: String
    )

    @Serializable
    private data class RunQueryRequest(
        @SerialName("structuredQuery") val structuredQuery: StructuredQuery
    )

    @Serializable
    private data class StructuredQuery(
        val from: List<CollectionSelector>,
        val where: Where,
        val orderBy: List<Order>,
        val limit: Int
    )

    @Serializable
    private data class CollectionSelector(
        @SerialName("collectionId") val collectionId: String
    )

    @Serializable
    private data class Order(
        val field: FieldReference,
        val direction: String = "ASCENDING"
    )

    @Serializable
    private data class FieldReference(
        @SerialName("fieldPath") val fieldPath: String
    )

    @Serializable
    private data class Where(
        @SerialName("fieldFilter") val fieldFilter: FieldFilter? = null,
        @SerialName("compositeFilter") val compositeFilter: CompositeFilter? = null
    )

    @Serializable
    private data class CompositeFilter(
        val op: String,
        val filters: List<Filter>
    )

    @Serializable
    private data class Filter(
        @SerialName("fieldFilter") val fieldFilter: FieldFilter? = null
    )

    @Serializable
    private data class FieldFilter(
        val field: FieldReference,
        val op: String,
        val value: FirestoreValue
    )

    @Serializable
    private data class RunQueryResponse(
        val document: FirestoreDocument? = null
    )

    @Serializable
    private data class FirestoreDocument(
        val name: String? = null,
        val fields: Map<String, FirestoreValue>? = null
    )

    @Serializable
    private data class FirestoreCreateDocumentRequest(
        val fields: Map<String, FirestoreValue>
    )

    @Serializable
    private data class FirestoreValue(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("integerValue") val integerValue: String? = null,
        @SerialName("timestampValue") val timestampValue: String? = null
    )

    private companion object {
        const val FIELD_CONVERSATION_ID = "conversation_id"
        const val FIELD_SENDER_ID = "sender_id"
        const val FIELD_BODY = "body"
        const val FIELD_CREATED_AT = "created_at"
        const val FIELD_STATUS = "status"
        const val FIELD_LOCAL_TEMP_ID = "local_temp_id"
    }
}
