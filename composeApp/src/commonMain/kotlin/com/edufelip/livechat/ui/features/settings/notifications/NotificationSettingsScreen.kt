package com.edufelip.livechat.ui.features.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationOptionCard
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationResetCard
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSectionHeader
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSettingsHeader
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationToggleCard
import com.edufelip.livechat.ui.features.settings.notifications.components.QuietHoursCard
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotificationSettingsScreen(
    state: NotificationSettingsUiState,
    modifier: Modifier = Modifier,
    systemPermissionGranted: Boolean = true,
    onBack: () -> Unit = {},
    onTogglePush: (Boolean) -> Unit = {},
    onEditSound: () -> Unit = {},
    onToggleQuietHours: (Boolean) -> Unit = {},
    onEditQuietHours: () -> Unit = {},
    onToggleVibration: (Boolean) -> Unit = {},
    onToggleMessagePreview: (Boolean) -> Unit = {},
    onResetNotifications: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val settings = state.settings
    val allowEdits = !state.isUpdating && !state.isLoading
    val soundLabel =
        settings.sound.takeIf { it.isNotBlank() }
            ?: strings.notifications.soundOptionPopcorn
    val permissionHint =
        if (systemPermissionGranted) {
            null
        } else {
            strings.notifications.permissionDisabledHint
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        NotificationSettingsHeader(
            title = strings.notifications.screenTitle,
            backContentDescription = strings.general.dismiss,
            onBack = onBack,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NotificationSectionHeader(title = strings.notifications.generalSection)

        NotificationToggleCard(
            title = strings.notifications.pushTitle,
            subtitle = strings.notifications.pushSubtitle,
            supportingText = permissionHint,
            checked = settings.pushEnabled,
            enabled = allowEdits,
            onCheckedChange = onTogglePush,
        )

        NotificationOptionCard(
            title = strings.notifications.soundTitle,
            value = soundLabel,
            enabled = allowEdits,
            onClick = onEditSound,
        )

        NotificationSectionHeader(title = strings.notifications.quietHoursSection)

        QuietHoursCard(
            title = strings.notifications.quietHoursTitle,
            subtitle = strings.notifications.quietHoursSubtitle,
            checked = settings.quietHoursEnabled,
            enabled = allowEdits,
            fromLabel = strings.notifications.quietHoursFromLabel,
            toLabel = strings.notifications.quietHoursToLabel,
            fromTime = formatDisplayTime(settings.quietHours.from, strings.general.timePeriodAm, strings.general.timePeriodPm),
            toTime = formatDisplayTime(settings.quietHours.to, strings.general.timePeriodAm, strings.general.timePeriodPm),
            onCheckedChange = onToggleQuietHours,
            onEditQuietHours = if (allowEdits) onEditQuietHours else null,
        )

        NotificationSectionHeader(title = strings.notifications.advancedSection)

        NotificationToggleCard(
            title = strings.notifications.vibrationTitle,
            subtitle = null,
            supportingText = null,
            checked = settings.inAppVibration,
            enabled = allowEdits,
            onCheckedChange = onToggleVibration,
        )

        NotificationToggleCard(
            title = strings.notifications.previewTitle,
            subtitle = strings.notifications.previewSubtitle,
            supportingText = null,
            checked = settings.showMessagePreview,
            enabled = allowEdits,
            onCheckedChange = onToggleMessagePreview,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

        NotificationResetCard(
            title = strings.notifications.resetTitle,
            enabled = allowEdits,
            onClick = onResetNotifications,
        )
    }
}

private fun formatDisplayTime(
    raw: String,
    amLabel: String,
    pmLabel: String,
): String {
    val parts = raw.split(":")
    if (parts.size != 2) return raw
    val hour = parts[0].toIntOrNull() ?: return raw
    val minute = parts[1].toIntOrNull() ?: return raw
    if (hour !in 0..23 || minute !in 0..59) return raw
    val period = if (hour >= 12) pmLabel else amLabel
    val displayHour =
        when (val hour12 = hour % 12) {
            0 -> 12
            else -> hour12
        }
    val minuteText = minute.toString().padStart(2, '0')
    return "$displayHour:$minuteText $period"
}

@DevicePreviews
@Preview
@Composable
private fun NotificationSettingsScreenPreview() {
    LiveChatPreviewContainer {
        NotificationSettingsScreen(
            state = NotificationSettingsUiState(isLoading = false),
            systemPermissionGranted = false,
        )
    }
}
