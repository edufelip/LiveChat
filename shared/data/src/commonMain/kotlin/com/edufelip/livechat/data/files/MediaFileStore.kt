package com.edufelip.livechat.data.files

/**
 * Platform file helper for media transport.
 */
expect object MediaFileStore {
    fun configure(basePath: String)

    fun exists(path: String): Boolean

    fun delete(path: String)

    suspend fun readBytes(path: String): ByteArray?

    suspend fun saveBytes(
        prefix: String,
        extension: String,
        data: ByteArray,
    ): String
}
