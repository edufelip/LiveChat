package com.project.livechat.composeapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformColorSchemeStrategy(): PlatformColorSchemeStrategy {
    return remember { PastelColorSchemeStrategy() }
}
