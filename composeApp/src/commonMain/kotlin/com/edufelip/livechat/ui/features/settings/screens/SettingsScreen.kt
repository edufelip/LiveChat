package com.edufelip.livechat.ui.features.settings.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.resources.SettingsStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class SettingsSection {
    Account,
    Notifications,
    Appearance,
    Privacy,
}

fun SettingsSection.title(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountTitle
        SettingsSection.Notifications -> strings.notificationsTitle
        SettingsSection.Appearance -> strings.appearanceTitle
        SettingsSection.Privacy -> strings.privacyTitle
    }

fun SettingsSection.description(strings: SettingsStrings): String =
    when (this) {
        SettingsSection.Account -> strings.accountDescription
        SettingsSection.Notifications -> strings.notificationsDescription
        SettingsSection.Appearance -> strings.appearanceDescription
        SettingsSection.Privacy -> strings.privacyDescription
    }

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsNavigationRequest) -> Unit = {},
) {
    val strings = liveChatStrings()
    val settingsStrings = strings.settings
    val sections = remember { SettingsSection.values() }
    val sectionRequests =
        remember(settingsStrings) {
            sections.map { section ->
                val title = section.title(settingsStrings)
                val description = section.description(settingsStrings)
                val message = buildOpeningSectionMessage(settingsStrings.openingSectionTemplate, title)
                SettingsNavigationRequest(
                    section = section,
                    title = title,
                    description = description,
                    placeholderMessage = message,
                )
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Text(
            text = settingsStrings.screenTitle,
            style = MaterialTheme.typography.titleMedium,
        )
        sectionRequests.forEach { request ->
            SettingsSectionCard(
                request = request,
                onSectionSelected = onSectionSelected,
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    request: SettingsNavigationRequest,
    onSectionSelected: (SettingsNavigationRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onSectionSelectedState = rememberUpdatedState(onSectionSelected)
    val onClick = remember(request) { { onSectionSelectedState.value(request) } }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildOpeningSectionMessage(
    template: String,
    title: String,
): String =
    template
        .replace("%1\$s", title)
        .replace("%s", title)

@DevicePreviews
@Preview
@Composable
private fun SettingsScreenPreview() {
    LiveChatPreviewContainer {
        SettingsScreen()
    }
}
