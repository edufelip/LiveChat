package com.edufelip.livechat.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PhoneExistsResponse(
    val uid: String? = null,
    val exists: Boolean = false,
)

@Serializable
data class PhoneExistsManyResponse(
    val registered: List<String> = emptyList(),
    val matches: List<PhoneExistsMatchPayload> = emptyList(),
    val failed: List<String> = emptyList(),
    val partial: Boolean = false,
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
    val failedPhones: List<String> = emptyList(),
    val isPartial: Boolean = false,
)

data class PhoneExistsSingleResult(
    val exists: Boolean,
    val uid: String?,
)
