package com.edufelip.livechat.data.remote

import dev.gitlive.firebase.functions.FirebaseFunctions

suspend fun FirebaseFunctions.phoneExists(e164: String): Boolean {
    val callable = this.httpsCallable(PHONE_EXISTS_FUNCTION)
    val result = callable(mapOf(PHONE_PARAM to e164))
    val payload = runCatching { result.data<Map<String, Boolean>>() }.getOrNull()
    return payload?.get(PHONE_EXISTS_RESPONSE_FLAG) ?: false
}

suspend fun FirebaseFunctions.phoneExistsMany(phones: List<String>): List<String> {
    if (phones.isEmpty()) return emptyList()
    val callable = this.httpsCallable(PHONE_EXISTS_MANY_FUNCTION)
    val result = callable(mapOf(PHONES_PARAM to phones))
    val payload = runCatching { result.data<Map<String, Any?>>() }.getOrNull()
    val registered = payload?.get(PHONES_RESPONSE_KEY) as? List<*>
    return registered?.filterIsInstance<String>() ?: emptyList()
}

private const val PHONE_EXISTS_FUNCTION = "phoneExists"
private const val PHONE_PARAM = "phone"
private const val PHONE_EXISTS_RESPONSE_FLAG = "exists"
private const val PHONE_EXISTS_MANY_FUNCTION = "phoneExistsMany"
private const val PHONES_PARAM = "phones"
private const val PHONES_RESPONSE_KEY = "registered"
