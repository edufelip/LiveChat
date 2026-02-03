package com.edufelip.livechat.ui.platform

import android.os.Build
import com.edufelip.livechat.App

actual fun appVersionInfo(): AppVersionInfo =
    runCatching {
        val context = App.instance.applicationContext
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: ""
        val buildNumber =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        AppVersionInfo(
            versionName = versionName,
            buildNumber = buildNumber,
        )
    }.getOrDefault(AppVersionInfo(versionName = "", buildNumber = ""))
