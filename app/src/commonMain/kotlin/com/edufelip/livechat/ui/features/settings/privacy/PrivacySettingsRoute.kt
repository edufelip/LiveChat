package com.edufelip.livechat.ui.features.settings.privacy

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
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyLastSeenBottomSheet
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyOption
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberPrivacySettingsPresenter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PrivacySettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        PrivacySettingsScreen(
            state = previewState(),
            lastSeenSummary = strings.privacy.lastSeenNobody,
            modifier = modifier,
            onBack = onBack,
        )
        return
    }

    val presenter = rememberPrivacySettingsPresenter()
    val state by presenter.collectState()

    var activeSheet by remember { mutableStateOf(PrivacySheet.None) }
    var selectedLastSeen by remember { mutableStateOf(state.settings.lastSeenAudience) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showBlockedContacts by remember { mutableStateOf(false) }

    LaunchedEffect(state.settings.lastSeenAudience) {
        if (activeSheet == PrivacySheet.None) {
            selectedLastSeen = state.settings.lastSeenAudience
        }
    }

    LaunchedEffect(state.errorMessage) {
        showErrorDialog = state.errorMessage != null
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

    if (activeSheet == PrivacySheet.LastSeen) {
        val options =
            listOf(
                PrivacyOption(LastSeenAudience.Everyone, strings.privacy.lastSeenEveryone),
                PrivacyOption(LastSeenAudience.Contacts, strings.privacy.lastSeenContacts),
                PrivacyOption(LastSeenAudience.Nobody, strings.privacy.lastSeenNobody),
            )
        PrivacyLastSeenBottomSheet(
            title = strings.privacy.lastSeenTitle,
            description = strings.privacy.lastSeenSheetDescription,
            options = options,
            selectedId = selectedLastSeen,
            confirmLabel = strings.privacy.saveCta,
            confirmEnabled = !state.isUpdating && selectedLastSeen != state.settings.lastSeenAudience,
            onSelect = { selectedLastSeen = it },
            onDismiss = { activeSheet = PrivacySheet.None },
            onConfirm = {
                presenter.updateLastSeenAudience(selectedLastSeen)
                activeSheet = PrivacySheet.None
            },
        )
    }

    if (showBlockedContacts) {
        BlockedContactsRoute(
            modifier = modifier,
            onBack = { showBlockedContacts = false },
        )
        return
    }

    val lastSeenSummary =
        when (state.settings.lastSeenAudience) {
            LastSeenAudience.Everyone -> strings.privacy.lastSeenEveryone
            LastSeenAudience.Contacts -> strings.privacy.lastSeenContacts
            LastSeenAudience.Nobody -> strings.privacy.lastSeenNobody
        }

    PrivacySettingsScreen(
        state = state,
        lastSeenSummary = lastSeenSummary,
        modifier = modifier,
        onBack = onBack,
        onOpenBlockedContacts = { showBlockedContacts = true },
        onInvitePreferenceSelected = { preference ->
            if (preference != state.settings.invitePreference) {
                presenter.updateInvitePreference(preference)
            }
        },
        onOpenLastSeen = { activeSheet = PrivacySheet.LastSeen },
        onToggleReadReceipts = presenter::updateReadReceipts,
        onToggleShareUsageData = presenter::updateShareUsageData,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
    )
}

private enum class PrivacySheet {
    LastSeen,
    None,
}

private fun previewState(): PrivacySettingsUiState =
    PrivacySettingsUiState(
        isLoading = false,
        settings = PrivacySettings(lastSeenAudience = LastSeenAudience.Nobody),
    )

@DevicePreviews
@Preview
@Composable
private fun PrivacySettingsRoutePreview() {
    LiveChatPreviewContainer {
        PrivacySettingsRoute()
    }
}
