package com.project.livechat.composeapp.ui.components.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Typography helper for section captions used in lists and grouped layouts.
 */
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
    )
}

@DevicePreviews
@Preview
@Composable
private fun SectionHeaderPreview() {
    LiveChatPreviewContainer {
        SectionHeader(title = "Pinned")
    }
}
