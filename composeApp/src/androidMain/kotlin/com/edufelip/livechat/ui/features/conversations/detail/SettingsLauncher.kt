package com.edufelip.livechat.ui.features.conversations.detail

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.edufelip.livechat.App

actual fun openAppSettings() {
    val context = App.instance.applicationContext
    val intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    ContextCompat.startActivity(context, intent, null)
}
