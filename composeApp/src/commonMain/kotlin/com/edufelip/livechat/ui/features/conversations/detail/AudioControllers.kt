package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerController {
    val playingPath: StateFlow<String?>
    val isPlaying: StateFlow<Boolean>
    val progress: StateFlow<Float>
    val durationMillis: StateFlow<Long>
    val positionMillis: StateFlow<Long>
    suspend fun play(path: String)
    suspend fun stop()
}

@Composable
expect fun rememberAudioPlayerController(): AudioPlayerController
