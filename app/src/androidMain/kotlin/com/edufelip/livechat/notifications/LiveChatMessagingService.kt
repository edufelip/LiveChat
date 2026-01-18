package com.edufelip.livechat.notifications

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edufelip.livechat.MainActivity
import com.edufelip.livechat.R
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.domain.useCases.IsQuietModeActiveUseCase
import com.edufelip.livechat.ui.platform.AppForegroundTracker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val DEFAULT_VIBRATION_PATTERN = longArrayOf(0, 50, 40, 50)

class LiveChatMessagingService : FirebaseMessagingService() {
    private val notificationSettingsRepository = AndroidKoinBridge.notificationSettingsRepository()
    private val quietModeUseCase = IsQuietModeActiveUseCase()

    override fun onMessageReceived(message: RemoteMessage) {
        val defaults =
            NotificationDefaults(
                title = getString(R.string.notification_message_title),
                hiddenBody = getString(R.string.notification_message_hidden_body),
            )
        val payload = NotificationPayload.from(message, defaults)
        val settings =
            runBlocking {
                runCatching { notificationSettingsRepository.observeSettings().first() }
                    .getOrElse { NotificationSettings() }
            }

        if (!settings.pushEnabled) return
        if (isQuietModeActive(settings)) return

        val showPreview = settings.showMessagePreview
        val isSilent = NotificationSound.normalizeId(settings.sound) == NotificationSound.Silent.id
        val title = if (showPreview) payload.title.ifBlank { defaults.title } else defaults.title
        val body = if (showPreview) payload.body else defaults.hiddenBody

        if (AppForegroundTracker.isForeground.value) {
            InAppNotificationCenter.emit(
                InAppNotification(
                    title = title,
                    body = body,
                    conversationId = payload.conversationId,
                    messageId = payload.messageId,
                ),
            )
            if (!isSilent && settings.inAppVibration) {
                NotificationVibrationHelper.vibrate(this)
            }
        } else {
            showNotification(title, body, payload, settings, isSilent)
        }
    }

    override fun onNewToken(token: String) {
        // TODO: send token to backend when push registration is implemented.
    }

    private fun showNotification(
        title: String,
        body: String,
        payload: NotificationPayload,
        settings: NotificationSettings,
        isSilent: Boolean,
    ) {
        val channelId = LiveChatNotificationChannels.ensureChannel(this, settings)
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                payload.conversationId?.let { putExtra(NotificationIntentKeys.EXTRA_CONVERSATION_ID, it) }
                payload.senderName?.let { putExtra(NotificationIntentKeys.EXTRA_SENDER_NAME, it) }
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                payload.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setVibrate(if (isSilent) null else DEFAULT_VIBRATION_PATTERN)
        }

        val soundUri = if (isSilent) null else NotificationSoundResolver.resolve(this, settings.sound)
        if (soundUri == null) {
            builder.setSound(null)
            builder.setSilent(true)
        } else {
            builder.setSound(soundUri)
        }

        NotificationManagerCompat.from(this).notify(payload.notificationId, builder.build())
    }

    private fun isQuietModeActive(settings: NotificationSettings): Boolean {
        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        return quietModeUseCase(settings, currentTime)
    }

    private companion object
}

private data class NotificationDefaults(
    val title: String,
    val hiddenBody: String,
)

private data class NotificationPayload(
    val title: String,
    val body: String,
    val conversationId: String?,
    val senderName: String?,
    val messageId: String?,
) {
    val notificationId: Int = (messageId ?: conversationId ?: title).hashCode()

    companion object {
        fun from(
            message: RemoteMessage,
            defaults: NotificationDefaults,
        ): NotificationPayload {
            val data = message.data
            val title =
                data["title"]
                    ?: message.notification?.title
                    ?: defaults.title
            val body =
                data["body"]
                    ?: message.notification?.body
                    ?: defaults.hiddenBody
            val conversationId = data["conversation_id"] ?: data["conversationId"]
            val senderName = data["sender_name"] ?: data["senderName"]
            val messageId = data["message_id"] ?: data["messageId"]

            return NotificationPayload(
                title = title,
                body = body,
                conversationId = conversationId,
                senderName = senderName,
                messageId = messageId,
            )
        }
    }
}
