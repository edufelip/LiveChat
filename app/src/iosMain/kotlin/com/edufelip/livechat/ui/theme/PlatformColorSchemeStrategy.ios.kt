package com.edufelip.livechat.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformColorSchemeStrategy(): PlatformColorSchemeStrategy = remember { PastelColorSchemeStrategy() }
