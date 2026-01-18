package com.edufelip.livechat.ui.features.settings.notifications

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.domain.models.NotificationSound
import com.edufelip.livechat.notifications.NotificationPermissionState
import com.edufelip.livechat.notifications.rememberNotificationPermissionManager
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.audio.rememberSoundPlayer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationQuietHoursBottomSheet
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSoundBottomSheet
import com.edufelip.livechat.ui.features.settings.notifications.components.NotificationSoundOption
import com.edufelip.livechat.ui.platform.AppLifecycleObserver
import com.edufelip.livechat.ui.platform.openAppSettings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberNotificationSettingsPresenter
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotificationSettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    targetItemId: String? = null,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        NotificationSettingsScreen(
            state = previewState(),
            modifier = modifier,
            onBack = onBack,
            targetItemId = targetItemId,
        )
        return
    }

    val presenter = rememberNotificationSettingsPresenter()
    val state by presenter.collectState()

    val soundPlayer = rememberSoundPlayer()

    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.stop() }
    }

    val permissionManager = rememberNotificationPermissionManager()
    var permissionState by remember { mutableStateOf(permissionManager.status()) }
    val coroutineScope = rememberCoroutineScope()

    var activeSheet by remember { mutableStateOf(NotificationSheet.None) }
    val normalizedSound =
        remember(state.settings.sound) {
            NotificationSound.normalizeId(state.settings.sound)
        }
    var selectedSound by remember { mutableStateOf(normalizedSound) }
    var quietFrom by remember { mutableStateOf(state.settings.quietHours.from) }
    var quietTo by remember { mutableStateOf(state.settings.quietHours.to) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val soundOptions =
        remember(strings) {
            listOf(
                NotificationSoundOption(
                    id = NotificationSound.Popcorn.id,
                    label = strings.notifications.soundOptionPopcorn,
                ),
                NotificationSoundOption(
                    id = NotificationSound.Chime.id,
                    label = strings.notifications.soundOptionChime,
                ),
                NotificationSoundOption(
                    id = NotificationSound.Ripple.id,
                    label = strings.notifications.soundOptionRipple,
                ),
                NotificationSoundOption(
                    id = NotificationSound.Silent.id,
                    label = strings.notifications.soundOptionSilent,
                ),
            )
        }

    LaunchedEffect(Unit) {
        permissionState = permissionManager.refreshStatus()
    }

    AppLifecycleObserver(
        onForeground = {
            coroutineScope.launch {
                permissionState = permissionManager.refreshStatus()
            }
        },
        onBackground = {},
    )

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
    }

    LaunchedEffect(activeSheet) {
        when (activeSheet) {
            NotificationSheet.Sound -> selectedSound = normalizedSound
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
            onSelect = {
                selectedSound = it
                soundPlayer.playNotificationSound(it)
            },
            onDismiss = {
                activeSheet = NotificationSheet.None
                soundPlayer.stop()
            },
            onConfirm = {
                presenter.updateSound(selectedSound)
                activeSheet = NotificationSheet.None
                soundPlayer.stop()
            },
            confirmEnabled = !state.isUpdating && selectedSound != normalizedSound,
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
        targetItemId = targetItemId,
        onTogglePush = { enabled ->
            if (!enabled) {
                presenter.updatePushNotifications(false)
                return@NotificationSettingsScreen
            }

            coroutineScope.launch {
                val result = permissionManager.requestPermission()
                permissionState = result
                if (result is NotificationPermissionState.Granted) {
                    presenter.updatePushNotifications(true)
                }
            }
        },
        onEditSound = { activeSheet = NotificationSheet.Sound },
        onToggleQuietHours = presenter::updateQuietHoursEnabled,
        onEditQuietHours = { activeSheet = NotificationSheet.QuietHours },
        onToggleVibration = presenter::updateInAppVibration,
        onToggleMessagePreview = presenter::updateShowMessagePreview,
        onResetNotifications = presenter::resetNotificationSettings,
        onOpenSystemSettings = { openAppSettings() },
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
