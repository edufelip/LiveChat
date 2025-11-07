package com.project.livechat.composeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class LiveChatSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    val gutter: Dp = 20.dp,
)

val LocalLiveChatSpacing = staticCompositionLocalOf { LiveChatSpacing() }

val MaterialTheme.spacing: LiveChatSpacing
    @Composable
    get() = LocalLiveChatSpacing.current
