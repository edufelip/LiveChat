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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationOptionCard
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationResetCard
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSectionHeader
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSettingsHeader
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationToggleCard
import com.edufelip.livechat.ui.features.settings.notifications.components.QuietHoursCard
import com.edufelip.livechat.ui.resources.NotificationsStrings
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
    onOpenSystemSettings: (() -> Unit)? = null,
) {
    val strings = liveChatStrings()
    val notificationStrings = strings.notifications
    val generalStrings = strings.general
    val settings = state.settings
    val allowEdits = !state.isUpdating && !state.isLoading
    val soundLabel =
        remember(settings.sound, notificationStrings) {
            notificationStrings.labelForSound(settings.sound)
        }
    val permissionHint =
        if (systemPermissionGranted) {
            null
        } else {
            notificationStrings.permissionDisabledHint
        }
    val scrollState = rememberScrollState()
    val onBackAction = rememberStableAction(onBack)
    val onTogglePushAction = rememberStableAction(onTogglePush)
    val onEditSoundAction = rememberStableAction(onEditSound)
    val onToggleQuietHoursAction = rememberStableAction(onToggleQuietHours)
    val onEditQuietHoursAction = rememberStableAction(onEditQuietHours)
    val onToggleVibrationAction = rememberStableAction(onToggleVibration)
    val onToggleMessagePreviewAction = rememberStableAction(onToggleMessagePreview)
    val onResetNotificationsAction = rememberStableAction(onResetNotifications)
    val onOpenSystemSettingsAction = rememberStableAction(onOpenSystemSettings ?: {})
    val quietHoursPresentation =
        remember(settings.quietHours, generalStrings) {
            QuietHoursPresentation(
                fromTime =
                    formatDisplayTime(
                        settings.quietHours.from,
                        generalStrings.timePeriodAm,
                        generalStrings.timePeriodPm,
                    ),
                toTime =
                    formatDisplayTime(
                        settings.quietHours.to,
                        generalStrings.timePeriodAm,
                        generalStrings.timePeriodPm,
                    ),
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        NotificationSettingsHeader(
            title = notificationStrings.screenTitle,
            backContentDescription = generalStrings.dismiss,
            onBack = onBackAction,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NotificationSectionHeader(title = notificationStrings.generalSection)

        NotificationToggleCard(
            title = notificationStrings.pushTitle,
            subtitle = notificationStrings.pushSubtitle,
            supportingText = permissionHint,
            checked = settings.pushEnabled,
            enabled = allowEdits,
            onCheckedChange = onTogglePushAction,
        )
        if (!systemPermissionGranted && onOpenSystemSettings != null) {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onOpenSystemSettingsAction,
                enabled = allowEdits,
            ) {
                Text(generalStrings.openSettings)
            }
        }

        NotificationOptionCard(
            title = notificationStrings.soundTitle,
            value = soundLabel,
            enabled = allowEdits,
            onClick = onEditSoundAction,
        )

        NotificationSectionHeader(title = notificationStrings.quietHoursSection)

        QuietHoursCard(
            title = notificationStrings.quietHoursTitle,
            subtitle = notificationStrings.quietHoursSubtitle,
            checked = settings.quietHoursEnabled,
            enabled = allowEdits,
            fromLabel = notificationStrings.quietHoursFromLabel,
            toLabel = notificationStrings.quietHoursToLabel,
            fromTime = quietHoursPresentation.fromTime,
            toTime = quietHoursPresentation.toTime,
            onCheckedChange = onToggleQuietHoursAction,
            onEditQuietHours = if (allowEdits) onEditQuietHoursAction else null,
        )

        NotificationSectionHeader(title = notificationStrings.advancedSection)

        NotificationToggleCard(
            title = notificationStrings.vibrationTitle,
            subtitle = null,
            supportingText = null,
            checked = settings.inAppVibration,
            enabled = allowEdits,
            onCheckedChange = onToggleVibrationAction,
        )

        NotificationToggleCard(
            title = notificationStrings.previewTitle,
            subtitle = notificationStrings.previewSubtitle,
            supportingText = null,
            checked = settings.showMessagePreview,
            enabled = allowEdits,
            onCheckedChange = onToggleMessagePreviewAction,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

        NotificationResetCard(
            title = notificationStrings.resetTitle,
            enabled = allowEdits,
            onClick = onResetNotificationsAction,
        )
    }
}

private fun NotificationsStrings.labelForSound(soundId: String): String {
    return when (NotificationSound.normalizeId(soundId)) {
        NotificationSound.Popcorn.id -> soundOptionPopcorn
        NotificationSound.Chime.id -> soundOptionChime
        NotificationSound.Ripple.id -> soundOptionRipple
        NotificationSound.Silent.id -> soundOptionSilent
        else -> soundOptionPopcorn
    }
}

private data class QuietHoursPresentation(
    val fromTime: String,
    val toTime: String,
)

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

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
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
