package com.edufelip.livechat.data.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual object MediaFileStore {
    private val ioDispatcher = Dispatchers.IO
    private var baseDir: File? = null

    actual fun configure(basePath: String) {
        baseDir = File(basePath).apply { mkdirs() }
    }

    actual fun exists(path: String): Boolean = File(path).exists()

    actual fun delete(path: String) {
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

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
            val targetDir =
                baseDir ?: File(System.getProperty("java.io.tmpdir"), "livechat-media").apply { mkdirs() }
            val temp = File.createTempFile(prefix, ".$extension", targetDir)
            temp.writeBytes(data)
            temp.absolutePath
        }
}
