package com.edufelip.livechat.ui.util

internal data class UiTestOverrides(
    val phone: String?,
    val otp: String?,
    val resetOnboarding: Boolean,
)

internal expect fun isUiTestMode(): Boolean

internal expect fun uiTestOverrides(): UiTestOverrides
