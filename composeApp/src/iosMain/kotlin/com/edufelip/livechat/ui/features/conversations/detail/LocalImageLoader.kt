package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import platform.Foundation.NSData
import org.jetbrains.skia.Image
import kotlinx.cinterop.memcpy
import kotlinx.cinterop.refTo

actual fun loadLocalImageBitmap(path: String): ImageBitmap? {
    val data = NSData.create(contentsOfFile = path) ?: return null
    val skiaImage =
        runCatching { Image.makeFromEncoded(data.toByteArray()) }.getOrNull() ?: return null
    return skiaImage.asImageBitmap()
}

private fun NSData.toByteArray(): ByteArray {
    val buffer = this.bytes ?: return ByteArray(0)
    val size = length.toInt()
    val byteArray = ByteArray(size)
    memcpy(byteArray.refTo(0), buffer, length)
    return byteArray
}
