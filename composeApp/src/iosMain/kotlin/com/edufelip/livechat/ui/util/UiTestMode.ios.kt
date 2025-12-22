package com.edufelip.livechat.ui.util

import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults

internal actual fun isUiTestMode(): Boolean {
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

internal actual fun uiTestOverrides(): UiTestOverrides {
    val environment = NSProcessInfo.processInfo.environment
    val defaults = NSUserDefaults.standardUserDefaults
    val phone = defaults.stringForKey("UITEST_PHONE") ?: environment["UITEST_PHONE"]?.toString()
    val otp = defaults.stringForKey("UITEST_OTP") ?: environment["UITEST_OTP"]?.toString()
    val resetFlag = defaults.stringForKey("UITEST_RESET_ONBOARDING") ?: environment["UITEST_RESET_ONBOARDING"]?.toString()
    val resetOnboarding = resetFlag == "1" || resetFlag == "true"
    return UiTestOverrides(
        phone = phone,
        otp = otp,
        resetOnboarding = resetOnboarding,
    )
}
