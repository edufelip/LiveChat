package com.edufelip.livechat.data.bridge

data class RemoteConfigBridgeError(
    val domain: String? = null,
    val code: Long? = null,
    val message: String? = null,
)

data class RemoteConfigBridgeResult(
    val activated: Boolean,
    val error: RemoteConfigBridgeError? = null,
)

interface RemoteConfigBridge {
    suspend fun fetchAndActivate(): RemoteConfigBridgeResult

    fun getString(key: String): String
}
