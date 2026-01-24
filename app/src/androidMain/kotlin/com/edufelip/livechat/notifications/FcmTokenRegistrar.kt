package com.edufelip.livechat.notifications

import android.content.Context
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.models.DevicePlatform
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Helper to register FCM token on app startup (when user is already logged in).
 */
object FcmTokenRegistrar {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun registerCurrentToken(context: Context) {
        scope.launch {
            try {
                // Get current FCM token
                val token = Tasks.await(FirebaseMessaging.getInstance().token)
                if (token.isNullOrBlank()) {
                    android.util.Log.w(TAG, "FCM token is blank, skipping registration")
                    return@launch
                }

                // Get device ID
                val prefs = context.getSharedPreferences("livechat_device", Context.MODE_PRIVATE)
                var deviceId = prefs.getString("device_id", null)
                if (deviceId == null) {
                    deviceId = java.util.UUID.randomUUID().toString()
                    prefs.edit().putString("device_id", deviceId).apply()
                }

                // Get app version
                val appVersion =
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (e: Exception) {
                        null
                    }

                // Register with backend
                val registerDeviceTokenUseCase = AndroidKoinBridge.registerDeviceTokenUseCase()
                registerDeviceTokenUseCase(
                    DeviceTokenRegistration(
                        deviceId = deviceId,
                        fcmToken = token,
                        platform = DevicePlatform.Android,
                        appVersion = appVersion,
                    ),
                )

                android.util.Log.d(TAG, "FCM token registered successfully")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to register FCM token on startup", e)
            }
        }
    }

    private const val TAG = "FcmTokenRegistrar"
}
