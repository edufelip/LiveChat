@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.ui.common.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

internal actual suspend fun loadAudioDurationMillis(path: String): Long =
    withContext(Dispatchers.Default) {
        val url = NSURL.fileURLWithPath(path)
        val player = AVAudioPlayer(contentsOfURL = url, error = null)
        val duration = player?.duration ?: 0.0
        (duration * 1000).toLong().coerceAtLeast(0L)
    }
