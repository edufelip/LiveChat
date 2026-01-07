package com.edufelip.livechat.ui.platform

import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

actual fun openWebViewUrl(url: String) {
    val target = NSURL(string = url) ?: return
    val controller = SFSafariViewController(uRL = target)
    topViewController()?.presentViewController(controller, animated = true, completion = null)
}

private fun topViewController(root: UIViewController? = currentRootViewController()): UIViewController? {
    val presented = root?.presentedViewController ?: return root
    return when (presented) {
        is UINavigationController -> topViewController(presented.visibleViewController)
        else -> topViewController(presented)
    }
}

private fun currentRootViewController(): UIViewController? {
    val windows = UIApplication.sharedApplication.windows as? List<*>
    val keyWindow = windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
    return (keyWindow ?: windows?.firstOrNull() as? UIWindow)?.rootViewController
}
