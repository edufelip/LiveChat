package com.edufelip.livechat.ui.platform

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
    context.startActivity(intent)
}
