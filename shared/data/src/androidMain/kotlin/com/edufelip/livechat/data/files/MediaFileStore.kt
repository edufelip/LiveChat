package com.edufelip.livechat.data.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.createTempFile

actual object MediaFileStore {
    private val ioDispatcher = Dispatchers.IO

    actual suspend fun readBytes(path: String): ByteArray? =
        withContext(ioDispatcher) {
            runCatching { File(path).takeIf { it.exists() }?.readBytes() }.getOrNull()
        }

    actual suspend fun saveBytes(
        prefix: String,
        extension: String,
        data: ByteArray,
    ): String =
        withContext(ioDispatcher) {
            val temp = createTempFile(prefix, ".$extension").toFile()
            temp.writeBytes(data)
            temp.absolutePath
        }
}
