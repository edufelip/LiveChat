package com.edufelip.livechat.ui.features.settings.notifications.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
internal fun NotificationSettingsHeader(
    title: String,
    backContentDescription: String,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = backContentDescription,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
internal fun NotificationSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun NotificationToggleCard(
    title: String,
    subtitle: String?,
    supportingText: String?,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val toggleModifier =
        if (enabled) {
            Modifier.toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
        } else {
            Modifier
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(toggleModifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        }
    }
}

@Composable
internal fun QuietHoursCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    fromLabel: String,
    toLabel: String,
    fromTime: String,
    toTime: String,
    onCheckedChange: (Boolean) -> Unit,
    onEditQuietHours: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = if (enabled) onCheckedChange else null,
                    enabled = enabled,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                QuietHoursTimeCard(
                    label = fromLabel,
                    value = fromTime,
                    enabled = enabled,
                    onClick = onEditQuietHours,
                    modifier = Modifier.weight(1f),
                )
                QuietHoursTimeCard(
                    label = toLabel,
                    value = toTime,
                    enabled = enabled,
                    onClick = onEditQuietHours,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun QuietHoursTimeCard(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isEnabled = enabled && onClick != null
    Card(
        modifier =
            modifier.then(
                if (isEnabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
@DevicePreviews
@Preview
private fun NotificationSettingsHeaderPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        NotificationSettingsHeader(
            title = strings.notifications.screenTitle,
            backContentDescription = strings.general.dismiss,
            onBack = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun NotificationSectionHeaderPreview() {
    LiveChatPreviewContainer {
        NotificationSectionHeader(title = liveChatStrings().notifications.generalSection)
    }
}

@DevicePreviews
@Preview
@Composable
private fun NotificationToggleCardPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        NotificationToggleCard(
            title = strings.notifications.pushTitle,
            subtitle = strings.notifications.pushSubtitle,
            supportingText = strings.notifications.permissionDisabledHint,
            checked = true,
            enabled = true,
            onCheckedChange = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun QuietHoursCardPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        QuietHoursCard(
            title = strings.notifications.quietHoursTitle,
            subtitle = strings.notifications.quietHoursSubtitle,
            checked = true,
            enabled = true,
            fromLabel = strings.notifications.quietHoursFromLabel,
            toLabel = strings.notifications.quietHoursToLabel,
            fromTime = strings.notifications.quietHoursFromPlaceholder,
            toTime = strings.notifications.quietHoursToPlaceholder,
            onCheckedChange = {},
            onEditQuietHours = {},
        )
    }
}
