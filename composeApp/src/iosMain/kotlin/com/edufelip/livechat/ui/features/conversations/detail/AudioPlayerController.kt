@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.Foundation.NSURL
import platform.darwin.NSObject

@Composable
actual fun rememberAudioPlayerController(): AudioPlayerController = remember { IosAudioPlayerController() }

private class IosAudioPlayerController : AudioPlayerController {
    private var player: AVAudioPlayer? = null
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
        withContext(Dispatchers.Default) {
            stopInternal()
            val url = NSURL.fileURLWithPath(path)
            val instance = AVAudioPlayer(contentsOfURL = url, error = null)
            if (instance == null) {
                stopInternal()
                return@withContext
            }
            player = instance
            instance.prepareToPlay()
            instance.play()
            _playingPath.value = path
            _isPlaying.value = true
            _durationMillis.value = (instance.duration * 1000).toLong()
            startProgressUpdates()
            instance.delegate =
                object : NSObject(), AVAudioPlayerDelegateProtocol {
                    override fun audioPlayerDidFinishPlaying(
                        player: AVAudioPlayer,
                        successfully: Boolean,
                    ) {
                        stopInternal()
                    }
                }
        }
    }

    override suspend fun stop() {
        withContext(Dispatchers.Default) {
            stopInternal()
        }
    }

    private fun stopInternal() {
        player?.stop()
        player = null
        _playingPath.value = null
        _isPlaying.value = false
        _progress.value = 0f
        _durationMillis.value = 0L
        _positionMillis.value = 0L
    }

    private fun startProgressUpdates() {
        CoroutineScope(Dispatchers.Default).launch {
            while (_isPlaying.value) {
                val current = player?.currentTime ?: 0.0
                val dur = _durationMillis.value
                val posMs = (current * 1000).toLong()
                _positionMillis.value = posMs
                _progress.value = if (dur > 0) posMs.toFloat() / dur.toFloat() else 0f
                delay(200)
            }
        }
    }
}
