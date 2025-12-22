package com.edufelip.livechat.data.util

import platform.Foundation.NSProcessInfo

internal fun isUiTestMode(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    val uiTestFlag = environment["UITEST_MODE"]?.toString()
    val xctestPath = environment["XCTestConfigurationFilePath"]?.toString()
    val arguments = NSProcessInfo.processInfo.arguments
    val hasUiTestArg = arguments.any { it?.toString() == "-ui-testing" }
    return uiTestFlag == "1" || !xctestPath.isNullOrBlank() || hasUiTestArg
}
