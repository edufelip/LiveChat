@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

actual fun loadLocalImageBitmap(path: String): ImageBitmap? {
    val data = NSData.create(contentsOfFile = path) ?: return null
    val skiaImage =
        runCatching { Image.makeFromEncoded(data.toByteArray()) }.getOrNull() ?: return null
    return skiaImage.toComposeImageBitmap()
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
