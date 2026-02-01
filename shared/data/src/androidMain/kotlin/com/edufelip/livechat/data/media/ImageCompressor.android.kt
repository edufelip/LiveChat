package com.edufelip.livechat.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

actual object ImageCompressor {
    actual fun compressJpeg(
        bytes: ByteArray,
        maxDimensionPx: Int,
        qualityPercent: Int,
    ): ByteArray? {
        return runCatching {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
            val originalWidth = boundsOptions.outWidth
            val originalHeight = boundsOptions.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return@runCatching null

            val sampleSize = calculateInSampleSize(originalWidth, originalHeight, maxDimensionPx)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return@runCatching null

            val resized = resizeIfNeeded(decoded, maxDimensionPx)
            ByteArrayOutputStream()
                .use { stream ->
                    resized.compress(Bitmap.CompressFormat.JPEG, qualityPercent.coerceIn(0, 100), stream)
                    stream.toByteArray()
                }.also {
                    if (resized !== decoded) {
                        resized.recycle()
                    }
                    decoded.recycle()
                }
        }.getOrNull()
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimensionPx: Int,
    ): Int {
        if (maxDimensionPx <= 0) return 1
        var inSampleSize = 1
        while (width / inSampleSize > maxDimensionPx || height / inSampleSize > maxDimensionPx) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun resizeIfNeeded(
        bitmap: Bitmap,
        maxDimensionPx: Int,
    ): Bitmap {
        if (maxDimensionPx <= 0) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val maxDimension = maxOf(width, height)
        if (maxDimension <= maxDimensionPx) return bitmap

        val scale = maxDimensionPx.toFloat() / maxDimension.toFloat()
        val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
