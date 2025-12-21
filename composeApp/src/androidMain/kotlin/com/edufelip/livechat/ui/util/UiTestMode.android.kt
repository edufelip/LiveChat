package com.edufelip.livechat.ui.util

internal actual fun isUiTestMode(): Boolean = false

internal actual fun uiTestOverrides(): UiTestOverrides = UiTestOverrides(null, null)
