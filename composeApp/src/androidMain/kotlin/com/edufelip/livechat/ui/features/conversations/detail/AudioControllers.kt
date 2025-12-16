package com.edufelip.livechat.ui.features.conversations.detail

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Composable
actual fun rememberAudioPlayerController(): AudioPlayerController =
    remember { AndroidAudioPlayerController() }

private class AndroidAudioPlayerController : AudioPlayerController {
    private val player = MediaPlayer()
    private val _playingPath = MutableStateFlow<String?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _progress = MutableStateFlow(0f)
    private val _durationMillis = MutableStateFlow(0L)
    private val _positionMillis = MutableStateFlow(0L)

    override val playingPath: StateFlow<String?> = _playingPath
    override val isPlaying: StateFlow<Boolean> = _isPlaying
    override val progress: StateFlow<Float> = _progress
    override val durationMillis: StateFlow<Long> = _durationMillis
    override val positionMillis: StateFlow<Long> = _positionMillis

    override suspend fun play(path: String) {
        withContext(Dispatchers.IO) {
            stopInternal()
            runCatching {
                player.reset()
                player.setDataSource(path)
                player.prepare()
                player.start()
                _playingPath.value = path
                _isPlaying.value = true
                _durationMillis.value = player.duration.toLong().coerceAtLeast(0)
                startProgressUpdates()
                player.setOnCompletionListener {
                    stopInternal()
                }
            }.onFailure {
                stopInternal()
            }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.IO) {
            stopInternal()
        }
    }

    private fun stopInternal() {
        if (_isPlaying.value) {
            runCatching { player.stop() }
        }
        _isPlaying.value = false
        _playingPath.value = null
        _progress.value = 0f
        _positionMillis.value = 0L
        _durationMillis.value = 0L
    }

    private fun startProgressUpdates() {
        // Simple polling; should be replaced with a better callback if available
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            while (_isPlaying.value) {
                val pos = runCatching { player.currentPosition }.getOrDefault(0)
                val dur = _durationMillis.value
                _positionMillis.value = pos.toLong()
                _progress.value = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
                kotlinx.coroutines.delay(200)
            }
        }
    }
}
