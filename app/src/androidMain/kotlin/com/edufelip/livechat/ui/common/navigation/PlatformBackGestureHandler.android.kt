package com.edufelip.livechat.ui.common.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackGestureHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // No-op on Android; system back/back handlers already handle navigation.
}
