@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.edufelip.livechat.data.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy
import platform.posix.rand

actual object MediaFileStore {
    private val ioDispatcher = Dispatchers.Default

    actual suspend fun readBytes(path: String): ByteArray? =
        withContext(ioDispatcher) {
            val data = NSData.create(contentsOfFile = path) ?: return@withContext null
            val buffer = data.bytes?.reinterpret<ByteVar>() ?: return@withContext null
            val result = ByteArray(data.length.toInt())
            result.usePinned {
                memcpy(it.addressOf(0), buffer, data.length)
            }
            result
        }

    actual suspend fun saveBytes(prefix: String, extension: String, data: ByteArray): String =
        withContext(ioDispatcher) {
            val tempDir = NSTemporaryDirectory() ?: "/tmp/"
            val fileName = "$prefix-${rand()}.$extension"
            val fullPath = tempDir + fileName
            data.usePinned { pinned ->
                val nsData = NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
                nsData.writeToFile(fullPath, atomically = true)
            }
            fullPath
        }
}
