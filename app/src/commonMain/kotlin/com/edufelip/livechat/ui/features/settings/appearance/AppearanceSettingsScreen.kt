package com.edufelip.livechat.ui.features.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.models.AppearanceSettingsUiState
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceSectionHeader
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceSettingsHeader
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceThemeCard
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceTypographyCard
import com.edufelip.livechat.ui.features.settings.components.settingsItemHighlight
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
fun AppearanceSettingsScreen(
    state: AppearanceSettingsUiState,
    sliderValue: Float,
    sampleScale: Float,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    targetItemId: String? = null,
    onThemeSelected: (ThemeMode) -> Unit = {},
    onTextScaleChange: (Float) -> Unit = {},
    onTextScaleChangeFinished: () -> Unit = {},
) {
    val strings = liveChatStrings()
    val appearanceStrings = strings.appearance
    val generalStrings = strings.general
    val settings = state.settings
    val allowEdits = !state.isLoading && !state.isUpdating
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val scrollState = rememberScrollState()
    val onBackAction = rememberStableAction(onBack)
    val onThemeSelectedAction = rememberStableAction(onThemeSelected)
    val onTextScaleChangeAction = rememberStableAction(onTextScaleChange)
    val onTextScaleChangeFinishedAction = rememberStableAction(onTextScaleChangeFinished)
    val themeOptions =
        remember(appearanceStrings) {
            listOf(
                ThemeOption(
                    mode = ThemeMode.System,
                    title = appearanceStrings.themeSystemTitle,
                    subtitle = appearanceStrings.themeSystemSubtitle,
                    icon = Icons.Rounded.BrightnessAuto,
                ),
                ThemeOption(
                    mode = ThemeMode.Light,
                    title = appearanceStrings.themeLightTitle,
                    subtitle = appearanceStrings.themeLightSubtitle,
                    icon = Icons.Rounded.LightMode,
                ),
                ThemeOption(
                    mode = ThemeMode.Dark,
                    title = appearanceStrings.themeDarkTitle,
                    subtitle = appearanceStrings.themeDarkSubtitle,
                    icon = Icons.Rounded.DarkMode,
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
        AppearanceSettingsHeader(
            title = appearanceStrings.screenTitle,
            backContentDescription = generalStrings.dismiss,
            onBack = onBackAction,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppearanceSectionHeader(title = appearanceStrings.themesSection)

        themeOptions.forEach { option ->
            val isSelected = settings.themeMode == option.mode
            val onClick =
                if (allowEdits) {
                    remember(option.mode, onThemeSelectedAction) { { onThemeSelectedAction(option.mode) } }
                } else {
                    null
                }
            val itemId =
                when (option.mode) {
                    ThemeMode.System -> "appearance_theme_system"
                    ThemeMode.Light -> "appearance_theme_light"
                    ThemeMode.Dark -> "appearance_theme_dark"
                }
            Box(modifier = Modifier.settingsItemHighlight(itemId, targetItemId)) {
                AppearanceThemeCard(
                    title = option.title,
                    subtitle = option.subtitle,
                    icon = option.icon,
                    iconTint = if (isSelected) selectedColor else unselectedColor,
                    selected = isSelected,
                    enabled = allowEdits,
                    onClick = onClick,
                )
            }
        }

        AppearanceSectionHeader(title = appearanceStrings.typographySection)

        Box(modifier = Modifier.settingsItemHighlight("appearance_text_scale", targetItemId)) {
            AppearanceTypographyCard(
                smallLabel = appearanceStrings.typographySmallLabel,
                defaultLabel = appearanceStrings.typographyDefaultLabel,
                largeLabel = appearanceStrings.typographyLargeLabel,
                sliderValue = sliderValue,
                enabled = allowEdits,
                onValueChange = onTextScaleChangeAction,
                onValueChangeFinished = onTextScaleChangeFinishedAction,
                sampleText = appearanceStrings.typographySample,
                sampleScale = sampleScale,
            )
        }
    }
}

private data class ThemeOption(
    val mode: ThemeMode,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

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
private fun AppearanceSettingsScreenPreview() {
    LiveChatPreviewContainer {
        AppearanceSettingsScreen(
            state = AppearanceSettingsUiState(isLoading = false),
            sliderValue = 50f,
            sampleScale = 1f,
        )
    }
}
