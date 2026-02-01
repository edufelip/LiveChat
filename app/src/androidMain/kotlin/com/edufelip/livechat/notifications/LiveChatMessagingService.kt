package com.edufelip.livechat.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.edufelip.livechat.MainActivity
import com.edufelip.livechat.R
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.models.DevicePlatform
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.notifications.InAppNotification
import com.edufelip.livechat.domain.notifications.InAppNotificationCenter
import com.edufelip.livechat.domain.useCases.IsQuietModeActiveUseCase
import com.edufelip.livechat.ui.platform.AppForegroundTracker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import java.util.Calendar

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
        val titleSource = payload.senderName?.takeIf { it.isNotBlank() } ?: payload.title
        val title = if (showPreview) titleSource.ifBlank { defaults.title } else defaults.title
        val body = if (showPreview) payload.body else defaults.hiddenBody

        val isForeground = AppForegroundTracker.isForeground.value
        Log.d(
            TAG,
            "onMessageReceived: app isForeground=$isForeground, showPreview=$showPreview",
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
        } else {
            Log.i(TAG, "onMessageReceived: app is background, showing system notification")
            val hasNotificationPermission =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
            if (hasNotificationPermission) {
                showNotification(title, body, payload)
            } else {
                Log.w(TAG, "onMessageReceived: missing POST_NOTIFICATIONS permission, skipping notification")
            }
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
    ) {
        Log.d(TAG, "showNotification: creating system notification (title=$title)")
        val channelId = LiveChatNotificationChannels.ensureChannel(this)
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
                .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(this).notify(payload.notificationId, builder.build())
        Log.i(TAG, "showNotification: system notification posted with id=${payload.notificationId}")
    }


    private fun isQuietModeActive(settings: NotificationSettings): Boolean {
        val calendar = Calendar.getInstance()
        val currentTime =
            LocalTime(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                second = calendar.get(Calendar.SECOND),
                nanosecond = calendar.get(Calendar.MILLISECOND) * 1_000_000,
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
