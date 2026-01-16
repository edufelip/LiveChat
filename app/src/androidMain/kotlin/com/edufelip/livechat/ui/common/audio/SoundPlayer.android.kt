package com.edufelip.livechat.ui.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null

    override fun playNotificationSound(soundName: String) {
        stop()

        if (soundName == "Silent") return

        // For now, we'll use the default notification sound as a placeholder
        // In a real app, we would map "Popcorn", "Chime", etc., to raw resource files
        val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

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
                    prepare()
                    start()
                    setOnCompletionListener {
                        stop()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember(context) { AndroidSoundPlayer(context) }
}
