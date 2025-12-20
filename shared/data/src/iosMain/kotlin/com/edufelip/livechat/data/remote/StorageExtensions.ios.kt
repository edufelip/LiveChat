@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.edufelip.livechat.data.remote

import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.posix.memcpy

/**
 * iOS implementation uses gitlive Firebase storage APIs.
 */
actual suspend fun FirebaseStorage.uploadBytes(objectPath: String, data: ByteArray): String {
    val ref = reference.child(objectPath)
    ref.putData(Data(data.toNSData()))
    return ref.toString()
}

actual suspend fun FirebaseStorage.downloadBytes(remoteUrl: String, maxSize: Long): ByteArray {
    val url =
        if (remoteUrl.startsWith(HTTP_PREFIX) || remoteUrl.startsWith(HTTPS_PREFIX)) {
            remoteUrl
        } else {
            referenceFromRemoteUrl(remoteUrl).getDownloadUrl()
        }
    return withContext(Dispatchers.Default) {
        val nsUrl = NSURL.URLWithString(url) ?: error("Invalid download url: $url")
        val data = NSData.create(contentsOfURL = nsUrl) ?: error("Unable to download data")
        val length = data.length.toLong()
        require(length <= maxSize) { "Downloaded data exceeds max size: $length > $maxSize" }
        data.toByteArray()
    }
}

actual suspend fun FirebaseStorage.deleteRemote(remoteUrl: String) {
    referenceFromRemoteUrl(remoteUrl).delete()
}

private fun FirebaseStorage.referenceFromRemoteUrl(remoteUrl: String) =
    if (remoteUrl.startsWith(GS_SCHEME)) {
        val path = remoteUrl.removePrefix(GS_SCHEME).substringAfter('/', "")
        if (path.isBlank()) reference else reference(path)
    } else {
        reference(remoteUrl)
    }

private const val GS_SCHEME = "gs://"
private const val HTTP_PREFIX = "http://"
private const val HTTPS_PREFIX = "https://"

private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }

private fun NSData.toByteArray(): ByteArray {
    if (length == 0UL) return ByteArray(0)
    val buffer = bytes?.reinterpret<ByteVar>() ?: return ByteArray(0)
    val result = ByteArray(length.toInt())
    result.usePinned {
        memcpy(it.addressOf(0), buffer, length)
    }
    return result
}
