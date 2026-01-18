package com.edufelip.livechat.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics

private class AndroidAnalyticsController(
    private val analytics: FirebaseAnalytics,
) : AnalyticsController {
    override fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }
}

@Composable
actual fun rememberAnalyticsController(): AnalyticsController {
    val context = LocalContext.current
    return remember(context) {
        AndroidAnalyticsController(FirebaseAnalytics.getInstance(context))
    }
}
