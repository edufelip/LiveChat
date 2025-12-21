package com.edufelip.livechat.data.bridge

interface MediaStorageBridge {
    suspend fun uploadBytes(
        objectPath: String,
        bytes: ByteArray,
    ): String

    suspend fun downloadBytes(
        remoteUrl: String,
        maxBytes: Long,
    ): ByteArray

    suspend fun deleteRemote(remoteUrl: String)
}
