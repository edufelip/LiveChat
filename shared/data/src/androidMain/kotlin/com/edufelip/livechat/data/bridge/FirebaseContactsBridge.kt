package com.edufelip.livechat.data.bridge

import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.remote.PhoneExistsBatchResult
import com.edufelip.livechat.data.remote.PhoneExistsManyResponse
import com.edufelip.livechat.data.remote.PhoneExistsMatch
import com.edufelip.livechat.data.remote.PhoneExistsMatchPayload
import com.edufelip.livechat.data.remote.PhoneExistsResponse
import com.edufelip.livechat.data.remote.PhoneExistsSingleResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseContactsBridge(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val config: FirebaseRestConfig,
) : ContactsRemoteBridge {
    override suspend fun phoneExists(phoneE164: String): PhoneExistsSingleResult {
        val callable = functions.getHttpsCallable(PHONE_EXISTS_FUNCTION)
        val result = callable.call(mapOf(PHONE_PARAM to phoneE164)).await()
        val payload = parsePhoneExistsResponse(result.data)

        return PhoneExistsSingleResult(
            exists = payload.exists,
            uid = payload.uid,
        )
    }

    override suspend fun phoneExistsMany(phones: List<String>): PhoneExistsBatchResult {
        if (phones.isEmpty()) return PhoneExistsBatchResult(emptyList(), emptyList())
        val callable = functions.getHttpsCallable(PHONE_EXISTS_MANY_FUNCTION)
        val result = callable.call(mapOf(PHONES_PARAM to phones)).await()
        val payload = parsePhoneExistsManyResponse(result.data)

        val matches =
            payload.matches.mapNotNull { match ->
                val phone = match.phone ?: return@mapNotNull null
                val uid = match.uid ?: return@mapNotNull null
                PhoneExistsMatch(phone = phone, uid = uid)
            }

        return PhoneExistsBatchResult(
            registeredPhones = payload.registered,
            matches = matches,
            failedPhones = payload.failed,
            isPartial = payload.partial,
        )
    }

    override suspend fun isUserRegistered(phoneE164: String): Boolean {
        val snap =
            firestore
                .collection(config.usersCollection)
                .whereEqualTo(PHONE_NUMBER_FIELD, phoneE164)
                .limit(1)
                .get()
                .await()
        return !snap.isEmpty
    }

    private fun parsePhoneExistsResponse(data: Any?): PhoneExistsResponse {
        val map = data as? Map<*, *> ?: return PhoneExistsResponse()
        val exists = map[FIELD_EXISTS] as? Boolean ?: false
        val uid = map[FIELD_UID] as? String
        return PhoneExistsResponse(uid = uid, exists = exists)
    }

    private fun parsePhoneExistsManyResponse(data: Any?): PhoneExistsManyResponse {
        val map = data as? Map<*, *> ?: return PhoneExistsManyResponse()
        val registered =
            (map[FIELD_REGISTERED] as? List<*>)
                ?.mapNotNull { it as? String }
                .orEmpty()
        val failed =
            (map[FIELD_FAILED] as? List<*>)
                ?.mapNotNull { it as? String }
                .orEmpty()
        val partial = map[FIELD_PARTIAL] as? Boolean ?: false
        val matches =
            (map[FIELD_MATCHES] as? List<*>)
                ?.mapNotNull { item ->
                    val match = item as? Map<*, *> ?: return@mapNotNull null
                    PhoneExistsMatchPayload(
                        phone = match[FIELD_PHONE] as? String,
                        uid = match[FIELD_UID] as? String,
                    )
                }.orEmpty()
        return PhoneExistsManyResponse(
            registered = registered,
            matches = matches,
            failed = failed,
            partial = partial,
        )
    }

    private companion object {
        const val PHONE_NUMBER_FIELD = "phone_num"
        const val PHONE_EXISTS_FUNCTION = "phoneExists"
        const val PHONE_PARAM = "phone"
        const val PHONE_EXISTS_MANY_FUNCTION = "phoneExistsMany"
        const val PHONES_PARAM = "phones"
        const val FIELD_EXISTS = "exists"
        const val FIELD_UID = "uid"
        const val FIELD_REGISTERED = "registered"
        const val FIELD_MATCHES = "matches"
        const val FIELD_PHONE = "phone"
        const val FIELD_FAILED = "failed"
        const val FIELD_PARTIAL = "partial"
    }
}
