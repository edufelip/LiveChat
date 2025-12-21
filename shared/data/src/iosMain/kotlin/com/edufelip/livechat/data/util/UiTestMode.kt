package com.edufelip.livechat.data.util

import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults

internal fun isUiTestMode(): Boolean {
    val environment = NSProcessInfo.processInfo.environment
    val uiTestFlag = environment["UITEST_MODE"]?.toString()
    val xctestPath = environment["XCTestConfigurationFilePath"]?.toString()
    val simulatorName = environment["SIMULATOR_DEVICE_NAME"]?.toString()
    val arguments = NSProcessInfo.processInfo.arguments
    val hasUiTestArg = arguments.any { it?.toString() == "-ui-testing" }
    val storedFlag = NSUserDefaults.standardUserDefaults.boolForKey("UITEST_MODE")
    val isSimulator = !simulatorName.isNullOrBlank()
    return storedFlag || uiTestFlag == "1" || !xctestPath.isNullOrBlank() || hasUiTestArg || isSimulator
}
