package com.edufelip.livechat.ui.util

internal data class UiTestOverrides(
    val phone: String?,
    val otp: String?,
)

internal expect fun isUiTestMode(): Boolean

internal expect fun uiTestOverrides(): UiTestOverrides
