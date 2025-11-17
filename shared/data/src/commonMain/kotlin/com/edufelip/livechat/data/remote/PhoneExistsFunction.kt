package com.edufelip.livechat.data.remote

import dev.gitlive.firebase.functions.FirebaseFunctions
import kotlinx.serialization.Serializable

suspend fun FirebaseFunctions.phoneExists(e164: String): PhoneExistsSingleResult {
    val callable = this.httpsCallable(PHONE_EXISTS_FUNCTION)
    val result = callable(mapOf(PHONE_PARAM to e164))
    val payload =
        runCatching { result.data<PhoneExistsResponse>() }
            .getOrElse { PhoneExistsResponse() }

    return PhoneExistsSingleResult(
        exists = payload.exists,
        uid = payload.uid,
    )
}

suspend fun FirebaseFunctions.phoneExistsMany(phones: List<String>): PhoneExistsBatchResult {
    if (phones.isEmpty()) return PhoneExistsBatchResult(emptyList(), emptyList())
    val callable = this.httpsCallable(PHONE_EXISTS_MANY_FUNCTION)
    val result = callable(mapOf(PHONES_PARAM to phones))
    val payload =
        runCatching { result.data<PhoneExistsManyResponse>() }
            .getOrElse { PhoneExistsManyResponse() }

    val matches =
        payload.matches.mapNotNull { match ->
            val phone = match.phone ?: return@mapNotNull null
            val uid = match.uid ?: return@mapNotNull null
            PhoneExistsMatch(phone = phone, uid = uid)
        }

    return PhoneExistsBatchResult(payload.registered, matches)
}

private const val PHONE_EXISTS_FUNCTION = "phoneExists"
private const val PHONE_PARAM = "phone"
private const val PHONE_EXISTS_RESPONSE_FLAG = "exists"
private const val PHONE_EXISTS_UID_FIELD = "uid"
private const val PHONE_EXISTS_MANY_FUNCTION = "phoneExistsMany"
private const val PHONES_PARAM = "phones"
private const val PHONES_RESPONSE_KEY = "registered"
private const val PHONES_MATCHES_KEY = "matches"

@Serializable
data class PhoneExistsResponse(
    val uid: String? = null,
    val exists: Boolean = false,
)

@Serializable
data class PhoneExistsManyResponse(
    val registered: List<String> = emptyList(),
    val matches: List<PhoneExistsMatchPayload> = emptyList(),
)

@Serializable
data class PhoneExistsMatchPayload(
    val phone: String? = null,
    val uid: String? = null,
)

data class PhoneExistsMatch(
    val phone: String,
    val uid: String,
)

data class PhoneExistsBatchResult(
    val registeredPhones: List<String>,
    val matches: List<PhoneExistsMatch>,
)

data class PhoneExistsSingleResult(
    val exists: Boolean,
    val uid: String?,
)
