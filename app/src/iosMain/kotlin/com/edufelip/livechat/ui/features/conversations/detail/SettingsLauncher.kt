package com.edufelip.livechat.ui.features.conversations.detail

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun openAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    if (url != null) {
        UIApplication.sharedApplication.openURL(url)
    }
}
