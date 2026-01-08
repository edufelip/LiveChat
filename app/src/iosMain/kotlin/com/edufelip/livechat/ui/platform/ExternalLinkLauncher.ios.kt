package com.edufelip.livechat.ui.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openExternalUrl(url: String) {
    val target = NSURL(string = url)
    if (target != null) {
        UIApplication.sharedApplication.openURL(target)
    }
}
