package com.edufelip.livechat.notifications

import android.content.Context
import android.util.Log
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.models.DevicePlatform
import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual object PlatformTokenRegistrar {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        Log.i(TAG, "initialize: PlatformTokenRegistrar initialized with context")
    }

    actual fun registerCurrentToken() {
        val context =
            applicationContext ?: run {
                Log.w(TAG, "registerCurrentToken: applicationContext is null, skipping registration")
                return
            }
        Log.i(TAG, "registerCurrentToken: starting FCM token registration")
        scope.launch {
            try {
                Log.d(TAG, "registerCurrentToken: fetching current FCM token from FirebaseMessaging")
                // Get current FCM token
                val token = Tasks.await(FirebaseMessaging.getInstance().token)
                Log.d(TAG, "registerCurrentToken: fetched token length=${token?.length ?: 0}, isBlank=${token.isNullOrBlank()}")
                if (token.isNullOrBlank()) {
                    Log.w(TAG, "registerCurrentToken: FCM token is blank, skipping registration")
                    return@launch
                }

                Log.d(TAG, "registerCurrentToken: preparing device ID and app version")
                // Get device ID
                val prefs = context.getSharedPreferences("livechat_device", Context.MODE_PRIVATE)
                var deviceId = prefs.getString("device_id", null)
                if (deviceId == null) {
                    deviceId =
                        java.util.UUID
                            .randomUUID()
                            .toString()
                    prefs.edit().putString("device_id", deviceId).apply()
                    Log.d(TAG, "registerCurrentToken: generated new deviceId=$deviceId")
                } else {
                    Log.d(TAG, "registerCurrentToken: using existing deviceId=$deviceId")
                }

                // Get app version
                val appVersion =
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName.also {
                            Log.d(TAG, "registerCurrentToken: appVersion=$it")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "registerCurrentToken: failed to get app version", e)
                        null
                    }

                // Register with backend
                Log.d(TAG, "registerCurrentToken: calling registerDeviceTokenUseCase (deviceId=$deviceId, platform=Android)")
                val registerDeviceTokenUseCase = AndroidKoinBridge.registerDeviceTokenUseCase()
                registerDeviceTokenUseCase(
                    DeviceTokenRegistration(
                        deviceId = deviceId,
                        fcmToken = token,
                        platform = DevicePlatform.Android,
                        appVersion = appVersion,
                    ),
                )

                Log.i(TAG, "registerCurrentToken: FCM token successfully registered for deviceId=$deviceId")
            } catch (e: Exception) {
                Log.e(TAG, "registerCurrentToken: Failed to register FCM token on startup", e)
            }
        }
    }

    private const val TAG = "FCM"
}
