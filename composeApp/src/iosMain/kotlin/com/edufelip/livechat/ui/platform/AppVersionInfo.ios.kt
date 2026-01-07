package com.edufelip.livechat.ui.platform

import platform.Foundation.NSBundle

actual fun appVersionInfo(): AppVersionInfo {
    val bundle = NSBundle.mainBundle
    val versionName = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0"
    val buildNumber = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "1"
    return AppVersionInfo(versionName = versionName, buildNumber = buildNumber)
}
