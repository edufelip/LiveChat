package com.edufelip.livechat.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import androidx.compose.ui.tooling.preview.Preview

/**
 * Generic row shell with trailing actions, used by lists across the app.
 */
@Composable
fun RowWithActions(
    title: String,
    subtitle: String,
    subtitleContent: (@Composable () -> Unit)? = null,
    endContent: @Composable () -> Unit,
    onClick: () -> Unit,
    highlight: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (highlight) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val tonalElevation = if (highlight) MaterialTheme.spacing.xxs else 0.dp

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { role = Role.Button }
                .heightIn(min = 48.dp)
                .padding(vertical = MaterialTheme.spacing.xs)
                .clickable(enabled = enabled, onClick = onClick),
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = tonalElevation,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleContent != null) {
                    subtitleContent()
                } else {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            endContent()
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun RowWithActionsPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        RowWithActions(
            title = strings.home.chatsTab,
            subtitle = strings.conversation.emptyList,
            endContent = { Text(strings.general.ok, style = MaterialTheme.typography.bodySmall) },
            onClick = {},
            enabled = true,
        )
    }
}
