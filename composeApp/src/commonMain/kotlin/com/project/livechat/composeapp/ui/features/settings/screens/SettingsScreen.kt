package com.project.livechat.composeapp.ui.features.settings.screens

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
import androidx.compose.ui.Modifier
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.resources.SettingsStrings
import com.project.livechat.composeapp.ui.resources.liveChatStrings
import com.project.livechat.composeapp.ui.theme.spacing
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
    onSectionSelected: (SettingsSection) -> Unit = {},
) {
    val strings = liveChatStrings()
    val sections = remember { SettingsSection.values() }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Text(
            text = strings.settings.screenTitle,
            style = MaterialTheme.typography.titleMedium,
        )
        sections.forEach { section ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSectionSelected(section) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = section.title(strings.settings),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = section.description(strings.settings),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun SettingsScreenPreview() {
    LiveChatPreviewContainer {
        SettingsScreen()
    }
}
