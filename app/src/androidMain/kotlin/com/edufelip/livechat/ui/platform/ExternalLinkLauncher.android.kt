package com.edufelip.livechat.ui.platform

import android.content.Intent
import android.net.Uri
import com.edufelip.livechat.App

actual fun openExternalUrl(url: String) {
    val context = App.instance.applicationContext
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(intent)
}
