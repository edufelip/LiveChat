package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.utils.canonicalPhoneNumber
import com.edufelip.livechat.domain.utils.currentEpochMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.functions.FirebaseFunctions
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FirebaseRestContactsRemoteData(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val config: FirebaseRestConfig,
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IContactsRemoteData {
    override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> =
        flow {
            if (!config.isConfigured || phoneContacts.isEmpty()) return@flow
            val defaultRegion = config.defaultRegionIso ?: "US"

            val canonicalContacts =
                phoneContacts.mapNotNull { contact ->
                    val canonical = canonicalPhoneNumber(contact.phoneNo, defaultRegion)
                    canonical.takeIf { it.isNotBlank() }?.let { canonical to contact }
                }

            if (canonicalContacts.isEmpty()) return@flow

            if (canonicalContacts.size >= BATCH_THRESHOLD) {
                emitBatchResults(canonicalContacts)
            } else {
                emitSingleResults(canonicalContacts)
            }
        }.flowOn(dispatcher)

    override suspend fun inviteContact(contact: Contact): Boolean =
        withContext(dispatcher) {
            if (!config.isConfigured) return@withContext false
            val now = currentEpochMillis()
            val documentsUrl = "${config.documentsEndpoint}/${config.invitesCollection}"
            val request =
                CreateDocumentRequest(
                    fields =
                        mapOf(
                            "phone_no" to Value(stringValue = contact.phoneNo),
                            "name" to Value(stringValue = contact.name),
                            "invited_at" to Value(integerValue = now.toString()),
                        ),
                )

            runCatching {
                httpClient.post(documentsUrl) {
                    if (config.apiKey.isNotBlank()) {
                        parameter("key", config.apiKey)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }.isSuccess
        }

    private suspend fun queryFirestoreDirectly(e164: String): Boolean {
        val snap =
            firestore
                .collection(config.usersCollection)
                .where { PHONE_NUMBER_FIELD equalTo e164 }
                .limit(1)
                .get()
        return snap.documents.isNotEmpty()
    }

    private suspend fun FlowCollector<Contact>.emitBatchResults(
        canonicalContacts: List<Pair<String, Contact>>,
    ) {
        val canonicalPhones = canonicalContacts.map { it.first }
        val result =
            runCatching {
                withTimeout(FUNCTION_TIMEOUT_MS) {
                    functions.phoneExistsMany(canonicalPhones)
                }
            }.getOrElse { PhoneExistsBatchResult(emptyList(), emptyList()) }
        val registeredPhones = result.registeredPhones.toSet()
        val matchByPhone = result.matches.associateBy { it.phone }

        canonicalContacts.forEach { (canonical, contact) ->
            val exists =
                if (registeredPhones.contains(canonical)) {
                    true
                } else {
                    runCatching { queryFirestoreDirectly(canonical) }.getOrDefault(false)
                }
            if (exists) {
                val matchedUid = matchByPhone[canonical]?.uid
                emit(contact.copy(isRegistered = true, firebaseUid = matchedUid ?: contact.firebaseUid))
            }
        }
    }

    private suspend fun FlowCollector<Contact>.emitSingleResults(canonicalContacts: List<Pair<String, Contact>>) {
        canonicalContacts.forEach { (canonical, contact) ->
            val result =
                runCatching {
                    withTimeout(FUNCTION_TIMEOUT_MS) {
                        functions.phoneExists(canonical)
                    }
                }.getOrNull()
            val exists =
                result?.exists ?: runCatching { queryFirestoreDirectly(canonical) }.getOrDefault(false)
            if (exists) {
                emit(contact.copy(isRegistered = true, firebaseUid = result?.uid ?: contact.firebaseUid))
            }
        }
    }

    @Serializable
    private data class CreateDocumentRequest(
        val fields: Map<String, Value>,
    )

    @Serializable
    private data class Value(
        @SerialName("stringValue") val stringValue: String? = null,
        @SerialName("integerValue") val integerValue: String? = null,
    )

    private companion object {
        const val PHONE_NUMBER_FIELD = "phone_num"
        const val FUNCTION_TIMEOUT_MS = 5_000L
        const val BATCH_THRESHOLD = 25
    }
}
