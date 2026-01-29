package com.edufelip.livechat.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edufelip.livechat.MainActivity
import com.edufelip.livechat.R
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.models.DevicePlatform
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.domain.notifications.InAppNotification
import com.edufelip.livechat.domain.notifications.InAppNotificationCenter
import com.edufelip.livechat.domain.useCases.IsQuietModeActiveUseCase
import com.edufelip.livechat.ui.platform.AppForegroundTracker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import java.time.LocalTime as JavaLocalTime

val DEFAULT_VIBRATION_PATTERN = longArrayOf(0, 50, 40, 50)

class LiveChatMessagingService : FirebaseMessagingService() {
    private val notificationSettingsRepository = AndroidKoinBridge.notificationSettingsRepository()
    private val registerDeviceTokenUseCase = AndroidKoinBridge.registerDeviceTokenUseCase()
    private val quietModeUseCase = IsQuietModeActiveUseCase()

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i(
            TAG,
            "onMessageReceived: received FCM message from=${message.from}, messageId=${message.messageId}",
        )
        Log.d(
            TAG,
            "onMessageReceived: notification=${message.notification != null}, data=${message.data}",
        )

        val defaults =
            NotificationDefaults(
                title = getString(R.string.notification_message_title),
                hiddenBody = getString(R.string.notification_message_hidden_body),
            )
        val payload = NotificationPayload.from(message, defaults)
        Log.d(
            TAG,
            "onMessageReceived: parsed payload - conversationId=${payload.conversationId}, " +
                "senderName=${payload.senderName}, messageId=${payload.messageId}",
        )

        val settings =
            runBlocking {
                runCatching { notificationSettingsRepository.observeSettings().first() }
                    .getOrElse {
                        Log.w(TAG, "onMessageReceived: failed to load notification settings, using defaults")
                        NotificationSettings()
                    }
            }

        if (!settings.pushEnabled) {
            Log.i(TAG, "onMessageReceived: push notifications disabled in settings, ignoring message")
            return
        }
        if (isQuietModeActive(settings)) {
            Log.i(TAG, "onMessageReceived: quiet mode is active, ignoring message")
            return
        }

        val showPreview = settings.showMessagePreview
        val isSilent = NotificationSound.normalizeId(settings.sound) == NotificationSound.Silent.id
        val titleSource = payload.senderName?.takeIf { it.isNotBlank() } ?: payload.title
        val title = if (showPreview) titleSource.ifBlank { defaults.title } else defaults.title
        val body = if (showPreview) payload.body else defaults.hiddenBody

        val isForeground = AppForegroundTracker.isForeground.value
        Log.d(
            TAG,
            "onMessageReceived: app isForeground=$isForeground, showPreview=$showPreview, isSilent=$isSilent",
        )

        if (isForeground) {
            Log.i(TAG, "onMessageReceived: app is foreground, emitting in-app notification")
            InAppNotificationCenter.emit(
                InAppNotification(
                    title = title,
                    body = body,
                    conversationId = payload.conversationId,
                    messageId = payload.messageId,
                ),
            )
            if (!isSilent && settings.inAppVibration) {
                Log.d(TAG, "onMessageReceived: triggering in-app vibration")
                NotificationVibrationHelper.vibrate(this)
            }
        } else {
            Log.i(TAG, "onMessageReceived: app is background, showing system notification")
            showNotification(title, body, payload, settings, isSilent)
        }
    }

    override fun onNewToken(token: String) {
        Log.i(TAG, "onNewToken: new FCM token received, length=${token.length}")
        Log.d(TAG, "onNewToken: token=$token")
        // Register token with backend
        runBlocking {
            runCatching {
                val deviceId = getStoredDeviceId()
                val appVersion = getAppVersion()
                Log.d(
                    TAG,
                    "onNewToken: registering token with backend (deviceId=$deviceId, appVersion=$appVersion)",
                )
                registerDeviceTokenUseCase(
                    DeviceTokenRegistration(
                        deviceId = deviceId,
                        fcmToken = token,
                        platform = DevicePlatform.Android,
                        appVersion = appVersion,
                    ),
                )
                Log.i(TAG, "onNewToken: FCM token registered successfully for deviceId=$deviceId")
            }.onFailure { error ->
                // Log error but don't crash
                Log.e(TAG, "onNewToken: Failed to register FCM token", error)
            }
        }
    }

    private fun getStoredDeviceId(): String {
        val prefs = getSharedPreferences("livechat_device", MODE_PRIVATE)
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
            Log.d(TAG, "getDeviceId: generated new deviceId=$deviceId")
        } else {
            Log.d(TAG, "getDeviceId: using existing deviceId=$deviceId")
        }
        return deviceId
    }

    private fun getAppVersion(): String? {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            Log.w(TAG, "getAppVersion: failed to get app version", e)
            null
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        title: String,
        body: String,
        payload: NotificationPayload,
        settings: NotificationSettings,
        isSilent: Boolean,
    ) {
        Log.d(TAG, "showNotification: creating system notification (title=$title, isSilent=$isSilent)")
        val channelId = LiveChatNotificationChannels.ensureChannel(this, settings)
        Log.d(TAG, "showNotification: using channelId=$channelId")

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                payload.conversationId?.let {
                    putExtra(NotificationIntentKeys.EXTRA_CONVERSATION_ID, it)
                    Log.d(TAG, "showNotification: added conversationId=$it to intent")
                }
                payload.senderName?.let {
                    putExtra(NotificationIntentKeys.EXTRA_SENDER_NAME, it)
                    Log.d(TAG, "showNotification: added senderName=$it to intent")
                }
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
            Log.d(TAG, "showNotification: notification is silent")
        } else {
            builder.setSound(soundUri)
            Log.d(TAG, "showNotification: notification sound=$soundUri")
        }

        NotificationManagerCompat.from(this).notify(payload.notificationId, builder.build())
        Log.i(TAG, "showNotification: system notification posted with id=${payload.notificationId}")
    }

    private fun isQuietModeActive(settings: NotificationSettings): Boolean {
        val now = JavaLocalTime.now()
        val currentTime =
            LocalTime(
                hour = now.hour,
                minute = now.minute,
                second = now.second,
                nanosecond = now.nano,
            )
        return quietModeUseCase(settings, currentTime)
    }

    private companion object {
        const val TAG = "FCM"
    }
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
