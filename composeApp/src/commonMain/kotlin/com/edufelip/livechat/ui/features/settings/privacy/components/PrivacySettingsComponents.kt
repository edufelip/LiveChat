package com.edufelip.livechat.ui.features.settings.privacy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PrivacySettingsHeader(
    title: String,
    subtitle: String?,
    backContentDescription: String,
    onBack: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = backContentDescription,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun PrivacyChevronCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: (() -> Unit)?,
) {
    val isEnabled = enabled && onClick != null
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (isEnabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint =
                    if (isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
            )
        }
    }
}

@Composable
internal fun PrivacySectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Column(
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
            content()
        }
    }
}

@Composable
internal fun PrivacyRadioOptionRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
                .padding(vertical = MaterialTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
        )
    }
}

@Composable
internal fun PrivacyToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val toggleModifier =
        if (enabled) {
            Modifier.clickable(
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) },
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
internal fun BlockedContactRow(
    contact: BlockedContact,
    fallbackLabel: String,
    unblockLabel: String,
    enabled: Boolean,
    onUnblock: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = contact.displayName ?: fallbackLabel,
                    style = MaterialTheme.typography.titleSmall,
                )
                val supportingText = contact.phoneNumber ?: contact.userId
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
            Text(
                text = unblockLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .clickable(enabled = enabled) { onUnblock(contact.userId) }
                        .padding(MaterialTheme.spacing.xs),
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacySettingsHeaderPreview() {
    val strings = liveChatStrings()
    LiveChatPreviewContainer {
        PrivacySettingsHeader(
            title = strings.privacy.screenTitle,
            subtitle = strings.privacy.screenSubtitle,
            backContentDescription = strings.general.dismiss,
            onBack = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacyChevronCardPreview() {
    LiveChatPreviewContainer {
        PrivacyChevronCard(
            title = "Blocked Contacts",
            subtitle = "View and manage people you've blocked",
            enabled = true,
            onClick = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacySectionCardPreview() {
    LiveChatPreviewContainer {
        PrivacySectionCard(
            title = "Invite Preferences",
            subtitle = "Control who can add you to groups and channels",
        ) {
            PrivacyRadioOptionRow(
                label = "Everyone",
                selected = true,
                enabled = true,
                onClick = {},
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun PrivacyToggleCardPreview() {
    LiveChatPreviewContainer {
        PrivacyToggleCard(
            title = "Read Receipts",
            subtitle = "If turned off, you won't see read receipts either.",
            checked = true,
            enabled = true,
            onCheckedChange = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun BlockedContactRowPreview() {
    LiveChatPreviewContainer {
        BlockedContactRow(
            contact = BlockedContact(userId = "user_123", displayName = "Alex Morgan", phoneNumber = "+1 555 123 4567"),
            fallbackLabel = "Unknown user",
            unblockLabel = "Unblock",
            enabled = true,
            onUnblock = {},
        )
    }
}
