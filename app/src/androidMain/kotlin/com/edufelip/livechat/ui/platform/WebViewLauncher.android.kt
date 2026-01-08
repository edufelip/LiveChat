package com.edufelip.livechat.ui.platform

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.edufelip.livechat.App

actual fun openWebViewUrl(url: String) {
    val context = App.instance.applicationContext
    val customTabs =
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .apply {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    customTabs.launchUrl(context, Uri.parse(url))
}
