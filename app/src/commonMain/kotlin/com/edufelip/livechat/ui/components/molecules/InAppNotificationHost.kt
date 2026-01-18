package com.edufelip.livechat.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edufelip.livechat.notifications.InAppNotification
import com.edufelip.livechat.notifications.InAppNotificationCenter
import com.edufelip.livechat.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private const val BANNER_DISPLAY_MS = 4000L

@Composable
fun InAppNotificationHost(
    modifier: Modifier = Modifier,
    onOpenConversation: (String) -> Unit = {},
) {
    var activeNotification by remember { mutableStateOf<InAppNotification?>(null) }

    LaunchedEffect(Unit) {
        InAppNotificationCenter.events.collectLatest { notification ->
            activeNotification = notification
            delay(BANNER_DISPLAY_MS)
            activeNotification = null
        }
    }

    AnimatedVisibility(
        visible = activeNotification != null,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier,
    ) {
        val notification = activeNotification ?: return@AnimatedVisibility
        InAppNotificationBanner(
            title = notification.title,
            message = notification.body,
            onClick = {
                notification.conversationId?.let(onOpenConversation)
                activeNotification = null
            },
        )
    }
}

@Composable
private fun InAppNotificationBanner(
    title: String,
    message: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .statusBarsPadding()
                .padding(
                    horizontal = MaterialTheme.spacing.gutter,
                    vertical = MaterialTheme.spacing.sm,
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        tonalElevation = MaterialTheme.spacing.xs,
        shadowElevation = MaterialTheme.spacing.xs,
    ) {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        PaddingValues(
                            horizontal = MaterialTheme.spacing.md,
                            vertical = MaterialTheme.spacing.sm,
                        ),
                    ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
