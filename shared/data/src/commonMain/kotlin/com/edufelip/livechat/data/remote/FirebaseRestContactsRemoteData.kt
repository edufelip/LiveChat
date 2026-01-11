package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.bridge.ContactsRemoteBridge
import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.utils.canonicalPhoneNumber
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.domain.utils.normalizePhoneNumber
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
    private val contactsBridge: ContactsRemoteBridge,
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

            emitBatchResults(canonicalContacts)
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

    private suspend fun FlowCollector<Contact>.emitBatchResults(canonicalContacts: List<Pair<String, Contact>>) {
        val canonicalPhones = canonicalContacts.map { it.first }.distinct()
        val registeredRemembered = mutableSetOf<String>()
        val matchByPhone = mutableMapOf<String, String>()
        val matchByNormalized = mutableMapOf<String, String>()

        canonicalPhones
            .chunked(BATCH_CHUNK_SIZE)
            .forEach { chunk ->
                val result =
                    runCatching {
                        withTimeout(FUNCTION_TIMEOUT_MS) {
                            contactsBridge.phoneExistsMany(chunk)
                        }
                    }.getOrElse { PhoneExistsBatchResult(emptyList(), emptyList()) }
                registeredRemembered.addAll(result.registeredPhones)
                result.matches.forEach { match ->
                    matchByPhone[match.phone] = match.uid
                    val normalized = normalizePhoneNumber(match.phone)
                    if (normalized.isNotBlank()) {
                        matchByNormalized[normalized] = match.uid
                    }
                }
            }

        val registeredNormalized =
            registeredRemembered
                .map { normalizePhoneNumber(it) }
                .filter { it.isNotBlank() }
                .toSet()

        canonicalContacts.forEach { (canonical, contact) ->
            val normalized = normalizePhoneNumber(canonical)
            val matchedUid = matchByPhone[canonical] ?: matchByNormalized[normalized]
            val exists = matchedUid != null || (normalized.isNotBlank() && normalized in registeredNormalized)
            if (!exists) return@forEach

            val resolvedUid =
                matchedUid
                    ?: contact.firebaseUid?.takeIf { it.isNotBlank() }
                    ?: resolveUid(canonical)
            if (!resolvedUid.isNullOrBlank()) {
                emit(contact.copy(isRegistered = true, firebaseUid = resolvedUid))
            }
        }
    }

    private suspend fun resolveUid(phoneE164: String): String? {
        val result =
            runCatching {
                withTimeout(FUNCTION_TIMEOUT_MS) {
                    contactsBridge.phoneExists(phoneE164)
                }
            }.getOrNull()
        return result?.uid?.takeIf { result.exists }
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
        const val FUNCTION_TIMEOUT_MS = 5_000L
        const val BATCH_CHUNK_SIZE = 100
    }
}
