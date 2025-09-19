package com.project.livechat.data.remote

import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.domain.models.Contact
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestContactsRemoteData(
    private val httpClient: HttpClient,
    private val config: FirebaseRestConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IContactsRemoteData {

    override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> = flow {
        if (!config.isConfigured) return@flow
        for (contact in phoneContacts) {
            val exists = runCatching {
                contactExists(contact.phoneNo)
            }.getOrDefault(false)
            if (exists) {
                emit(contact)
            }
        }
    }

    private suspend fun contactExists(phoneNumber: String): Boolean = withContext(dispatcher) {
        if (!config.isConfigured) return@withContext false
        val requestBody = RunQueryRequest(
            structuredQuery = StructuredQuery(
                from = listOf(CollectionSelector(collectionId = config.usersCollection)),
                where = Where(
                    FieldFilter(
                        field = FieldReference(fieldPath = PHONE_NUMBER_FIELD),
                        op = "EQUAL",
                        value = Value(stringValue = phoneNumber)
                    )
                ),
                limit = 1
            )
        )

        val responses: List<RunQueryResponse> = httpClient.post(config.endpoint) {
            if (config.apiKey.isNotBlank()) {
                parameter("key", config.apiKey)
            }
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()

        responses.any { it.document != null }
    }

    @Serializable
    private data class RunQueryRequest(
        @SerialName("structuredQuery") val structuredQuery: StructuredQuery
    )

    @Serializable
    private data class StructuredQuery(
        val from: List<CollectionSelector>,
        val where: Where,
        val limit: Int
    )

    @Serializable
    private data class CollectionSelector(
        @SerialName("collectionId") val collectionId: String
    )

    @Serializable
    private data class Where(
        @SerialName("fieldFilter") val fieldFilter: FieldFilter
    )

    @Serializable
    private data class FieldFilter(
        val field: FieldReference,
        val op: String,
        val value: Value
    )

    @Serializable
    private data class FieldReference(
        @SerialName("fieldPath") val fieldPath: String
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String
    )

    @Serializable
    private data class RunQueryResponse(
        val document: FirestoreDocument? = null
    )

    @Serializable
    private data class FirestoreDocument(
        val name: String? = null
    )

    private companion object {
        const val PHONE_NUMBER_FIELD = "phone_num"
    }
}
