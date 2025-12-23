package com.edufelip.livechat.data.util

import platform.Foundation.NSProcessInfo

internal fun isUiTestMode(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    val e2eFlag = environment["E2E_MODE"]?.toString()
    if (e2eFlag == "1" || e2eFlag == "true") {
        return false
    }
    val uiTestFlag = environment["UITEST_MODE"]?.toString()
    val xctestPath = environment["XCTestConfigurationFilePath"]?.toString()
    val arguments = NSProcessInfo.processInfo.arguments
    val hasUiTestArg = arguments.any { it?.toString() == "-ui-testing" }
    return uiTestFlag == "1" || !xctestPath.isNullOrBlank() || hasUiTestArg
}
