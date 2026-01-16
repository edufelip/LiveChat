package com.edufelip.livechat.ui.features.settings.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.notifications.NotificationPermissionState
import com.edufelip.livechat.notifications.rememberNotificationPermissionManager
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationQuietHoursBottomSheet
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSoundBottomSheet
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSoundOption
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberNotificationSettingsPresenter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotificationSettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        NotificationSettingsScreen(
            state = previewState(),
            modifier = modifier,
            onBack = onBack,
        )
        return
    }

    val presenter = rememberNotificationSettingsPresenter()
    val state by presenter.collectState()

    val permissionManager = rememberNotificationPermissionManager()
    var permissionState by remember { mutableStateOf(permissionManager.status()) }

    var activeSheet by remember { mutableStateOf(NotificationSheet.None) }
    var selectedSound by remember { mutableStateOf(state.settings.sound) }
    var quietFrom by remember { mutableStateOf(state.settings.quietHours.from) }
    var quietTo by remember { mutableStateOf(state.settings.quietHours.to) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val soundOptions =
        remember(strings) {
            listOf(
                NotificationSoundOption(
                    id = strings.notifications.soundOptionPopcorn,
                    label = strings.notifications.soundOptionPopcorn,
                ),
                NotificationSoundOption(
                    id = strings.notifications.soundOptionChime,
                    label = strings.notifications.soundOptionChime,
                ),
                NotificationSoundOption(
                    id = strings.notifications.soundOptionRipple,
                    label = strings.notifications.soundOptionRipple,
                ),
                NotificationSoundOption(
                    id = strings.notifications.soundOptionSilent,
                    label = strings.notifications.soundOptionSilent,
                ),
            )
        }

    LaunchedEffect(Unit) {
        permissionState = permissionManager.refreshStatus()
    }

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    LaunchedEffect(activeSheet) {
        when (activeSheet) {
            NotificationSheet.Sound -> selectedSound = state.settings.sound
            NotificationSheet.QuietHours -> {
                quietFrom = state.settings.quietHours.from
                quietTo = state.settings.quietHours.to
            }
            NotificationSheet.None -> Unit
        }
    }

    val errorMessage = state.errorMessage
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                presenter.clearError()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        presenter.clearError()
                    },
                ) {
                    Text(strings.general.ok)
                }
            },
            title = { Text(strings.general.errorTitle) },
            text = { Text(errorMessage) },
        )
    }

    if (activeSheet == NotificationSheet.Sound) {
        NotificationSoundBottomSheet(
            title = strings.notifications.soundSheetTitle,
            description = strings.notifications.soundSheetDescription,
            options = soundOptions,
            selectedId = selectedSound,
            onSelect = { selectedSound = it },
            onDismiss = { activeSheet = NotificationSheet.None },
            onConfirm = {
                presenter.updateSound(selectedSound)
                activeSheet = NotificationSheet.None
            },
            confirmEnabled = !state.isUpdating && selectedSound != state.settings.sound,
            confirmLabel = strings.notifications.saveCta,
        )
    }

    if (activeSheet == NotificationSheet.QuietHours) {
        NotificationQuietHoursBottomSheet(
            title = strings.notifications.quietHoursSheetTitle,
            description = strings.notifications.quietHoursSheetDescription,
            fromLabel = strings.notifications.quietHoursFromLabel,
            toLabel = strings.notifications.quietHoursToLabel,
            fromPlaceholder = strings.notifications.quietHoursFromPlaceholder,
            toPlaceholder = strings.notifications.quietHoursToPlaceholder,
            fromValue = quietFrom,
            toValue = quietTo,
            onFromChange = { quietFrom = it },
            onToChange = { quietTo = it },
            onDismiss = { activeSheet = NotificationSheet.None },
            onConfirm = {
                presenter.updateQuietHoursWindow(quietFrom, quietTo)
                activeSheet = NotificationSheet.None
            },
            confirmEnabled = !state.isUpdating && isValidTime(quietFrom) && isValidTime(quietTo),
            confirmLabel = strings.notifications.saveCta,
            isUpdating = state.isUpdating,
        )
    }

    // Enable back gesture support
    SettingsSubmenuBackHandler(
        enabled = true,
        onBack = onBack,
    )

    NotificationSettingsScreen(
        state = state,
        modifier = modifier,
        systemPermissionGranted = permissionState is NotificationPermissionState.Granted,
        onBack = onBack,
        onTogglePush = presenter::updatePushNotifications,
        onEditSound = { activeSheet = NotificationSheet.Sound },
        onToggleQuietHours = presenter::updateQuietHoursEnabled,
        onEditQuietHours = { activeSheet = NotificationSheet.QuietHours },
        onToggleVibration = presenter::updateInAppVibration,
        onToggleMessagePreview = presenter::updateShowMessagePreview,
        onResetNotifications = presenter::resetNotificationSettings,
    )
}

private enum class NotificationSheet {
    Sound,
    QuietHours,
    None,
}

private fun isValidTime(value: String): Boolean {
    val parts = value.split(":")
    if (parts.size != 2) return false
    val hour = parts[0].toIntOrNull() ?: return false
    val minute = parts[1].toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

private fun previewState(): NotificationSettingsUiState =
    NotificationSettingsUiState(
        isLoading = false,
        settings = NotificationSettings(),
    )

@DevicePreviews
@Preview
@Composable
private fun NotificationSettingsRoutePreview() {
    LiveChatPreviewContainer {
        NotificationSettingsRoute()
    }
}
