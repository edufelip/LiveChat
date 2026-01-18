package com.edufelip.livechat.ui.features.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/**
 * Composable modifier that adds highlight background to an item.
 *
 * Design decisions:
 * - Uses AnimatedColor for smooth transitions
 * - Auto-dismisses highlight after duration
 * - Clean separation: this handles only visual highlight, scroll handled separately
 *
 * @param itemId The ID of this item
 * @param targetItemId The ID of the item that should be highlighted
 * @param highlightColor The color to use for highlighting
 * @param durationMillis How long to show the highlight
 */
@Composable
fun Modifier.settingsItemHighlight(
    itemId: String,
    targetItemId: String?,
    highlightColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
    durationMillis: Int = 2000,
): Modifier {
    val shouldHighlight = itemId == targetItemId && targetItemId != null
    var isHighlighted by remember(shouldHighlight) { mutableStateOf(shouldHighlight) }

    // Auto-dismiss after duration
    LaunchedEffect(shouldHighlight) {
        if (shouldHighlight) {
            isHighlighted = true
            delay(durationMillis.toLong())
            isHighlighted = false
        }
    }

    val backgroundColor by
        animateColorAsState(
            targetValue = if (isHighlighted) highlightColor else Color.Transparent,
            animationSpec = tween(300),
            label = "settingsItemHighlight",
        )

    return this.then(Modifier.background(backgroundColor))
}

/**
 * Effect that scrolls to a target item when targetItemId changes.
 *
 * Design decisions:
 * - Uses LaunchedEffect for one-shot scrolling on item ID change
 * - findItemIndex provided as lambda to avoid coupling with specific list implementations
 * - Adds delay to ensure layout is complete before scrolling
 *
 * @param targetItemId The ID of the item to scroll to
 * @param listState The LazyListState to control
 * @param findItemIndex Lambda that returns the index of an item given its ID
 */
@Composable
fun SettingsScrollToItem(
    targetItemId: String?,
    listState: LazyListState,
    findItemIndex: (String) -> Int?,
) {
    LaunchedEffect(targetItemId) {
        if (targetItemId != null) {
            // Small delay to ensure layout is complete
            delay(100)
            val index = findItemIndex(targetItemId)
            if (index != null && index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }
}
