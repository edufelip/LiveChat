package com.edufelip.livechat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object LiveChatNotificationChannels {
    private const val CHANNEL_ID = "messages"

    fun ensureChannel(context: Context): String {
        val channelId = CHANNEL_ID
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
                enableVibration(true)
            }

        manager.createNotificationChannel(channel)
        return channelId
    }
}
