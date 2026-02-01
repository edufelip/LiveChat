package com.edufelip.livechat.ui.components.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import androidx.compose.ui.tooling.preview.Preview

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
        SectionHeader(title = liveChatStrings().conversation.pinnedSectionTitle)
    }
}
