package com.edufelip.livechat.analytics

import androidx.compose.runtime.Composable

interface AnalyticsController {
    fun setCollectionEnabled(enabled: Boolean)
}

@Composable
expect fun rememberAnalyticsController(): AnalyticsController
