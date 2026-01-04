package com.edufelip.livechat.ui.features.settings.appearance.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun AppearanceSettingsHeader(
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
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
internal fun AppearanceSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun AppearanceThemeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    selected: Boolean,
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .then(Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SelectionIndicator(selected = selected)
        }
    }
}

@Composable
internal fun AppearanceToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
            )
        }
    }
}

@Composable
internal fun AppearanceTypographyCard(
    smallLabel: String,
    defaultLabel: String,
    largeLabel: String,
    sliderValue: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    sampleText: String,
    sampleScale: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = smallLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = defaultLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = largeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = sliderValue,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                enabled = enabled,
                valueRange = 0f..100f,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    text = sampleText,
                    style = MaterialTheme.typography.bodyMedium.scale(sampleScale),
                    modifier = Modifier.padding(MaterialTheme.spacing.sm),
                )
            }
        }
    }
}

private fun TextStyle.scale(scale: Float): TextStyle =
    copy(
        fontSize = fontSize.scaleTextUnit(scale),
        lineHeight = lineHeight.scaleTextUnit(scale),
    )

private fun TextUnit.scaleTextUnit(scale: Float): TextUnit =
    if (isUnspecified) {
        this
    } else {
        (value * scale).sp
    }

@Composable
private fun SelectionIndicator(selected: Boolean) {
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun AppearanceSettingsHeaderPreview() {
    LiveChatPreviewContainer {
        AppearanceSettingsHeader(
            title = "Appearance",
            backContentDescription = "Back",
            onBack = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun AppearanceSectionHeaderPreview() {
    LiveChatPreviewContainer {
        AppearanceSectionHeader(title = "Themes")
    }
}

@DevicePreviews
@Preview
@Composable
private fun AppearanceThemeCardPreview() {
    LiveChatPreviewContainer {
        AppearanceThemeCard(
            title = "System default",
            subtitle = "Match your device settings",
            icon = Icons.Rounded.BrightnessAuto,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            selected = true,
            enabled = true,
            onClick = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun AppearanceToggleCardPreview() {
    LiveChatPreviewContainer {
        AppearanceToggleCard(
            title = "Reduce motion",
            subtitle = "Minimize animations",
            checked = true,
            enabled = true,
            onCheckedChange = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun AppearanceTypographyCardPreview() {
    LiveChatPreviewContainer {
        AppearanceTypographyCard(
            smallLabel = "Small",
            defaultLabel = "Default",
            largeLabel = "Large",
            sliderValue = 50f,
            enabled = true,
            onValueChange = {},
            onValueChangeFinished = {},
            sampleText = "The quick brown fox jumps over the lazy dog.",
            sampleScale = 1f,
        )
    }
}
