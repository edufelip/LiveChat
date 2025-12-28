package com.edufelip.livechat.data.media

/**
 * Platform image compression helper.
 */
expect object ImageCompressor {
    fun compressJpeg(
        bytes: ByteArray,
        maxDimensionPx: Int,
        qualityPercent: Int,
    ): ByteArray?
}
