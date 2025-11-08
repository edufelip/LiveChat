package com.edufelip.livechat.composeapp.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.composeapp.preview.DevicePreviews
import com.edufelip.livechat.composeapp.preview.LiveChatPreviewContainer
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Small status badge used across conversation and contact surfaces.
 */
@Composable
fun Badge(
    text: String,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier =
            Modifier
                .background(color = tint.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp))
                .semantics { contentDescription = text }
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = tint,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun BadgePreview() {
    LiveChatPreviewContainer {
        Badge(text = "Pinned")
    }
}
