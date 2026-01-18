package com.edufelip.livechat.ui.common.audio

import android.content.Context
import android.media.AudioAttributes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.edufelip.livechat.notifications.NotificationSoundResolver

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null

    override fun playNotificationSound(soundName: String) {
        stop()

        val notificationUri = NotificationSoundResolver.resolve(context, soundName) ?: return

        try {
            mediaPlayer =
                android.media.MediaPlayer().apply {
                    setDataSource(context, notificationUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    setOnPreparedListener { player -> player.start() }
                    setOnCompletionListener {
                        stop()
                    }
                    setOnErrorListener { _, _, _ ->
                        stop()
                        true
                    }
                    prepareAsync()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            player.release()
        }
        mediaPlayer = null
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember(context) { AndroidSoundPlayer(context) }
}
