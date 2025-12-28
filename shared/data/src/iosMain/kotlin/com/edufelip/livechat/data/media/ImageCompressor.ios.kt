@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.data.media

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.drawInRect
import platform.posix.memcpy
import kotlin.math.roundToInt

actual object ImageCompressor {
    actual fun compressJpeg(
        bytes: ByteArray,
        maxDimensionPx: Int,
        qualityPercent: Int,
    ): ByteArray? {
        return runCatching {
            val data = bytes.toNSData() ?: return@runCatching null
            val image = UIImage(data = data) ?: return@runCatching null
            val resized = resizeIfNeeded(image, maxDimensionPx)
            val quality = (qualityPercent.coerceIn(0, 100) / 100.0)
            val jpegData = UIImageJPEGRepresentation(resized, quality) ?: return@runCatching null
            jpegData.toByteArray()
        }.getOrNull()
    }

    private fun resizeIfNeeded(
        image: UIImage,
        maxDimensionPx: Int,
    ): UIImage {
        if (maxDimensionPx <= 0) return image
        val size = image.size
        val width = size.width
        val height = size.height
        val maxDimension = maxOf(width, height)
        if (maxDimension <= maxDimensionPx) return image

        val scale = maxDimensionPx / maxDimension
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
        val targetSize = platform.CoreGraphics.CGSizeMake(targetWidth.toDouble(), targetHeight.toDouble())

        UIGraphicsBeginImageContextWithOptions(targetSize, true, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, targetSize.width, targetSize.height))
        val scaled = UIGraphicsGetImageFromCurrentImageContext() ?: image
        UIGraphicsEndImageContext()
        return scaled
    }

    private fun ByteArray.toNSData(): NSData? =
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }

    private fun NSData.toByteArray(): ByteArray {
        val buffer = bytes?.reinterpret<ByteVar>() ?: return ByteArray(0)
        val result = ByteArray(length.toInt())
        result.usePinned {
            memcpy(it.addressOf(0), buffer, length)
        }
        return result
    }
}
