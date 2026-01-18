package com.edufelip.livechat.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class IosAnalyticsController : AnalyticsController {
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

@Composable
actual fun rememberAnalyticsController(): AnalyticsController {
    return remember { IosAnalyticsController() }
}
