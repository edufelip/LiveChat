@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.edufelip.livechat.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.posix.memcpy

actual suspend fun loadAvatarImageBitmap(
    photoUrl: String,
    platformContext: Any?,
): ImageBitmap? {
    return withContext(Dispatchers.Default) {
        runCatching {
            val url = NSURL.URLWithString(photoUrl) ?: return@runCatching null
            val data = fetchData(url) ?: return@runCatching null
            val skiaImage = Image.makeFromEncoded(data.toByteArray())
            skiaImage.toComposeImageBitmap()
        }.getOrNull()
    }
}

private suspend fun fetchData(url: NSURL): NSData? =
    withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
        NSData.create(contentsOfURL = url)
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

private const val REQUEST_TIMEOUT_MS = 10_000L
