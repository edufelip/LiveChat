package com.edufelip.livechat.ui.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.notifications.InAppNotification
import com.edufelip.livechat.domain.notifications.InAppNotificationCenter
import com.edufelip.livechat.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

private const val BANNER_DISPLAY_MS = 4000L
private val SWIPE_DISMISS_THRESHOLD = 48.dp

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
        DismissibleInAppNotificationBanner(
            title = notification.title,
            message = notification.body,
            onDismiss = { activeNotification = null },
            onClick = {
                notification.conversationId?.let(onOpenConversation)
                activeNotification = null
            },
        )
    }
}

@Composable
private fun DismissibleInAppNotificationBanner(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val dismissThresholdPx =
        with(LocalDensity.current) {
            SWIPE_DISMISS_THRESHOLD.toPx()
        }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = if (isDragging) snap() else tween(180),
        label = "inAppNotificationOffset",
    )
    val draggableState =
        rememberDraggableState { delta ->
            val next = dragOffsetY + delta
            dragOffsetY = if (next > 0f) 0f else next
        }

    Surface(
        modifier =
            modifier
                .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                .statusBarsPadding()
                .padding(
                    horizontal = MaterialTheme.spacing.gutter,
                    vertical = MaterialTheme.spacing.sm,
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStarted = { isDragging = true },
                    onDragStopped = {
                        val shouldDismiss = dragOffsetY <= -dismissThresholdPx
                        dragOffsetY = 0f
                        isDragging = false
                        if (shouldDismiss) onDismiss()
                    },
                )
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
