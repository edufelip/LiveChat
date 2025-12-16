package com.edufelip.livechat.data.files

/**
 * Platform file helper for media transport.
 */
expect object MediaFileStore {
    suspend fun readBytes(path: String): ByteArray?
    suspend fun saveBytes(prefix: String, extension: String, data: ByteArray): String
}

