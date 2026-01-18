package com.edufelip.livechat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSound

object LiveChatNotificationChannels {
    private const val CHANNEL_PREFIX = "messages"

    fun ensureChannel(
        context: Context,
        settings: NotificationSettings,
    ): String {
        val channelId = channelId(settings)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return channelId

        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) != null) return channelId

        val channel =
            NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "LiveChat message notifications"
                val isSilent = settings.sound == NotificationSound.Silent.id
                enableVibration(!isSilent)
                vibrationPattern = if (isSilent) null else DEFAULT_VIBRATION_PATTERN
                if (isSilent) {
                    setSound(null, null)
                } else {
                    val soundUri = NotificationSoundResolver.resolve(context, settings.sound)
                    val attributes =
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    setSound(soundUri, attributes)
                }
            }

        manager.createNotificationChannel(channel)
        return channelId
    }

    private fun channelId(settings: NotificationSettings): String {
        val soundSuffix = if (settings.sound == NotificationSound.Silent.id) "silent" else "sound"
        return "$CHANNEL_PREFIX_$soundSuffix"
    }
}
