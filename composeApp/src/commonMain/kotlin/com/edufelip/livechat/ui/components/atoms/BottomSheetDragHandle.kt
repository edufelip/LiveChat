package com.edufelip.livechat.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.ui.theme.spacing

@Composable
internal fun BottomSheetDragHandle(
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
    topPadding: Dp = MaterialTheme.spacing.sm,
    bottomPadding: Dp = MaterialTheme.spacing.md,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = topPadding, bottom = bottomPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(width)
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(color),
        )
    }
}
