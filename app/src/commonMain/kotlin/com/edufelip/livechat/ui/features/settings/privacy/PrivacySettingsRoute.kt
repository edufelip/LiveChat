package com.edufelip.livechat.ui.features.settings.privacy

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PrivacySettings
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyLastSeenBottomSheet
import com.edufelip.livechat.ui.features.settings.privacy.components.PrivacyOption
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberPrivacySettingsPresenter
import com.edufelip.livechat.ui.theme.LocalReduceMotion
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrivacySettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val reduceMotion = LocalReduceMotion.current

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
    var destination by rememberSaveable { mutableStateOf(PrivacyDestination.Main) }

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

    val lastSeenSummary =
        when (state.settings.lastSeenAudience) {
            LastSeenAudience.Everyone -> strings.privacy.lastSeenEveryone
            LastSeenAudience.Contacts -> strings.privacy.lastSeenContacts
            LastSeenAudience.Nobody -> strings.privacy.lastSeenNobody
        }

    // Enable back gesture support
    SettingsSubmenuBackHandler(
        enabled = true,
        onBack = {
            if (destination == PrivacyDestination.BlockedContacts) {
                destination = PrivacyDestination.Main
            } else {
                onBack()
            }
        },
    )

    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
            } else {
                val direction =
                    when {
                        targetState.animationOrder() > initialState.animationOrder() -> 1
                        targetState.animationOrder() < initialState.animationOrder() -> -1
                        else -> 0
                    }
                if (direction == 0) {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                } else {
                    (
                        slideInHorizontally(
                            animationSpec = tween(300),
                        ) { fullWidth -> fullWidth / 4 * direction } + fadeIn(animationSpec = tween(300))
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = tween(300),
                            ) { fullWidth -> -fullWidth / 4 * direction } + fadeOut(animationSpec = tween(200))
                        )
                }
            }
        },
        label = "privacy_navigation_transition",
    ) { target ->
        when (target) {
            PrivacyDestination.Main ->
                PrivacySettingsScreen(
                    state = state,
                    lastSeenSummary = lastSeenSummary,
                    modifier = modifier,
                    onBack = onBack,
                    onOpenBlockedContacts = { destination = PrivacyDestination.BlockedContacts },
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

            PrivacyDestination.BlockedContacts ->
                BlockedContactsRoute(
                    modifier = modifier,
                    onBack = { destination = PrivacyDestination.Main },
                )
        }
    }
}

private enum class PrivacySheet {
    LastSeen,
    None,
}

private enum class PrivacyDestination {
    Main,
    BlockedContacts,
}

private fun PrivacyDestination.animationOrder(): Int =
    when (this) {
        PrivacyDestination.Main -> 0
        PrivacyDestination.BlockedContacts -> 1
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
