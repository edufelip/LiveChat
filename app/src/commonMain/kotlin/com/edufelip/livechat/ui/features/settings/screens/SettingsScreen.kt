package com.edufelip.livechat.ui.features.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.SettingsTestTags
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.platform.appVersionInfo
import com.edufelip.livechat.ui.platform.isAndroid
import com.edufelip.livechat.ui.platform.openWebViewUrl
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
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    val sectionRequests = remember(settingsStrings) { buildSectionRequests(settingsStrings) }
    val onSectionSelectedAction = rememberStableAction(onSectionSelected)
    val rows =
        remember(sectionRequests, onSectionSelectedAction) {
            sectionRequests.map { request ->
                SettingsRowItem(
                    id = request.section.name,
                    title = request.title,
                    icon = request.icon(),
                    iconBackground = request.iconBackground(),
                    trailingIcon = Icons.Rounded.ChevronRight,
                    onClick = { onSectionSelectedAction(request) },
                )
            }
        }
    val privacyPolicyRow =
        remember(settingsStrings.privacyPolicyTitle, settingsStrings.privacyPolicyUrl) {
            SettingsRowItem(
                id = "privacy_policy",
                title = settingsStrings.privacyPolicyTitle,
                icon = Icons.Rounded.Shield,
                iconBackground = SettingsIconGray,
                trailingIcon = Icons.Rounded.OpenInNew,
                onClick = { openWebViewUrl(settingsStrings.privacyPolicyUrl) },
            )
        }
    val normalizedQuery = remember(searchQuery) { searchQuery.trim() }
    val filteredRows =
        remember(rows, normalizedQuery) {
            if (normalizedQuery.isBlank()) {
                rows
            } else {
                rows.filter { item ->
                    item.title.contains(normalizedQuery, ignoreCase = true)
                }
            }
        }
    val showPrivacyPolicy =
        remember(privacyPolicyRow.title, normalizedQuery) {
            normalizedQuery.isBlank() || privacyPolicyRow.title.contains(normalizedQuery, ignoreCase = true)
        }
    val appVersion = remember { appVersionInfo() }
    val appName =
        remember(settingsStrings) {
            if (isAndroid()) settingsStrings.footerAppNameAndroid else settingsStrings.footerAppNameIos
        }
    val versionLabel =
        remember(settingsStrings, appVersion) {
            formatVersionLabel(settingsStrings.versionFormat, appVersion.versionName, appVersion.buildNumber)
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        SettingsStatusBar(
            modifier =
                Modifier
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(top = MaterialTheme.spacing.lg),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = MaterialTheme.spacing.gutter)
                    .padding(top = MaterialTheme.spacing.md, bottom = MaterialTheme.spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {
            SettingsHeader(title = settingsStrings.screenTitle)
            SettingsSearchField(
                value = searchQuery,
                placeholder = settingsStrings.searchPlaceholder,
                onValueChange = { searchQuery = it },
                modifier = Modifier.testTag(SettingsTestTags.SEARCH_FIELD),
            )

            if (filteredRows.isNotEmpty()) {
                SettingsGroupCard(rows = filteredRows)
            }

            if (showPrivacyPolicy) {
                SettingsGroupCard(rows = listOf(privacyPolicyRow))
            }

            SettingsFooter(
                appName = appName,
                versionLabel = versionLabel,
            )
        }
    }
}

@Composable
private fun SettingsStatusBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "9:41",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
            Icon(
                imageVector = Icons.Rounded.SignalCellularAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp),
            )
            Icon(
                imageVector = Icons.Rounded.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp),
            )
            Icon(
                imageVector = Icons.Rounded.BatteryFull,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
private fun SettingsSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
        colors = colors,
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
private fun SettingsGroupCard(
    rows: List<SettingsRowItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                SettingsRow(item = row)
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = MaterialTheme.spacing.md),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsRowItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = item.onClick)
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconTile(
            icon = item.icon,
            backgroundColor = item.iconBackground,
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = item.trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsIconTile(
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(30.dp)
                .background(backgroundColor, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsFooter(
    appName: String,
    versionLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = versionLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

private data class SettingsRowItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconBackground: Color,
    val trailingIcon: ImageVector,
    val onClick: () -> Unit,
)

private fun buildSectionRequests(strings: SettingsStrings): List<SettingsNavigationRequest> {
    val sections = SettingsSection.values()
    return sections.map { section ->
        val title = section.title(strings)
        val description = section.description(strings)
        val message = buildOpeningSectionMessage(strings.openingSectionTemplate, title)
        SettingsNavigationRequest(
            section = section,
            title = title,
            description = description,
            placeholderMessage = message,
        )
    }
}

private fun SettingsNavigationRequest.icon(): ImageVector =
    when (section) {
        SettingsSection.Account -> Icons.Rounded.Person
        SettingsSection.Notifications -> Icons.Rounded.Notifications
        SettingsSection.Appearance -> Icons.Rounded.DarkMode
        SettingsSection.Privacy -> Icons.Rounded.Lock
    }

private fun SettingsNavigationRequest.iconBackground(): Color =
    when (section) {
        SettingsSection.Account -> SettingsIconBlue
        SettingsSection.Notifications -> SettingsIconRed
        SettingsSection.Appearance -> SettingsIconPurple
        SettingsSection.Privacy -> SettingsIconGreen
    }

private fun buildOpeningSectionMessage(
    template: String,
    title: String,
): String =
    template
        .replace("%1\$s", title)
        .replace("%s", title)

private fun formatVersionLabel(
    template: String,
    versionName: String,
    buildNumber: String,
): String =
    template
        .replace("%1\$s", versionName)
        .replace("%2\$s", buildNumber)
        .replace("%s", versionName)

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

private val SettingsIconBlue = Color(0xFF007AFF)
private val SettingsIconRed = Color(0xFFFF3B30)
private val SettingsIconPurple = Color(0xFF5856D6)
private val SettingsIconGreen = Color(0xFF34C759)
private val SettingsIconGray = Color(0xFF8E8E93)

@DevicePreviews
@Preview
@Composable
private fun SettingsScreenPreview() {
    LiveChatPreviewContainer {
        SettingsScreen()
    }
}
