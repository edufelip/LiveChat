package com.project.livechat.composeapp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.autoreleasepool
import platform.Foundation.NSStringFromClass
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIApplicationMain
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow

@OptIn(ExperimentalForeignApi::class)
fun main() {
    autoreleasepool {
        UIApplicationMain(
            argc = 0,
            argv = null,
            principalClassName = null,
            delegateClassName = NSStringFromClass(LiveChatAppDelegate),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@ExportObjCClass
class LiveChatAppDelegate : UIResponder(), UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    private var windowHolder: UIWindow? = null

    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?,
    ): Boolean {
        windowHolder =
            UIWindow(frame = UIScreen.mainScreen.bounds).apply {
                rootViewController = MainViewController()
                makeKeyAndVisible()
            }
        return true
    }
}
