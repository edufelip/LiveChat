package com.edufelip.livechat.ui.common.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState

@Composable
fun rememberAudioDurationMillis(path: String?): State<Long> =
    produceState(initialValue = 0L, key1 = path) {
        val safePath = path?.takeIf { it.isNotBlank() }
        value =
            if (safePath == null) {
                0L
            } else {
                runCatching { loadAudioDurationMillis(safePath) }.getOrDefault(0L)
            }
    }

internal expect suspend fun loadAudioDurationMillis(path: String): Long
