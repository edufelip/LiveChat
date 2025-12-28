@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.edufelip.livechat.data.files

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy
import platform.posix.rand

actual object MediaFileStore {
    private val ioDispatcher = Dispatchers.Default
    private var basePath: String? = null

    actual fun configure(basePath: String) {
        this.basePath = basePath
        ensureDirectory(basePath)
    }

    actual fun exists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)

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

    actual suspend fun saveBytes(
        prefix: String,
        extension: String,
        data: ByteArray,
    ): String =
        withContext(ioDispatcher) {
            val directory = resolveMediaDirectory()
            val fileName = "$prefix-${rand()}.$extension"
            val fullPath = "$directory/$fileName"
            data.usePinned { pinned ->
                val nsData = NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
                nsData.writeToFile(fullPath, atomically = true)
            }
            fullPath
        }

    private fun resolveMediaDirectory(): String {
        val resolved = basePath ?: (documentDirectory() + "/media")
        ensureDirectory(resolved)
        return resolved
    }

    private fun ensureDirectory(path: String) {
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
    }

    private fun documentDirectory(): String {
        val manager = NSFileManager.defaultManager
        val url: NSURL? =
            manager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
        return requireNotNull(url?.path) { "Unable to resolve iOS documents directory" }
    }
}
