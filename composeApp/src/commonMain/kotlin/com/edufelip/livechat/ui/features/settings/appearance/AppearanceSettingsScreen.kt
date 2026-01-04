package com.edufelip.livechat.ui.features.settings.appearance

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.AppearanceSettingsUiState
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceSectionHeader
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceSettingsHeader
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceThemeCard
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceToggleCard
import com.edufelip.livechat.ui.features.settings.appearance.components.AppearanceTypographyCard
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppearanceSettingsScreen(
    state: AppearanceSettingsUiState,
    sliderValue: Float,
    sampleScale: Float,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onThemeSelected: (ThemeMode) -> Unit = {},
    onTextScaleChange: (Float) -> Unit = {},
    onTextScaleChangeFinished: () -> Unit = {},
    onToggleReduceMotion: (Boolean) -> Unit = {},
    onToggleHighContrast: (Boolean) -> Unit = {},
) {
    val strings = liveChatStrings()
    val settings = state.settings
    val allowEdits = !state.isLoading && !state.isUpdating
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.gutter, vertical = MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        AppearanceSettingsHeader(
            title = strings.appearance.screenTitle,
            backContentDescription = strings.general.dismiss,
            onBack = onBack,
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppearanceSectionHeader(title = strings.appearance.themesSection)

        AppearanceThemeCard(
            title = strings.appearance.themeSystemTitle,
            subtitle = strings.appearance.themeSystemSubtitle,
            icon = Icons.Rounded.BrightnessAuto,
            iconTint = if (settings.themeMode == ThemeMode.System) selectedColor else unselectedColor,
            selected = settings.themeMode == ThemeMode.System,
            enabled = allowEdits,
            onClick = { onThemeSelected(ThemeMode.System) },
        )

        AppearanceThemeCard(
            title = strings.appearance.themeLightTitle,
            subtitle = strings.appearance.themeLightSubtitle,
            icon = Icons.Rounded.LightMode,
            iconTint = if (settings.themeMode == ThemeMode.Light) selectedColor else unselectedColor,
            selected = settings.themeMode == ThemeMode.Light,
            enabled = allowEdits,
            onClick = { onThemeSelected(ThemeMode.Light) },
        )

        AppearanceThemeCard(
            title = strings.appearance.themeDarkTitle,
            subtitle = strings.appearance.themeDarkSubtitle,
            icon = Icons.Rounded.DarkMode,
            iconTint = if (settings.themeMode == ThemeMode.Dark) selectedColor else unselectedColor,
            selected = settings.themeMode == ThemeMode.Dark,
            enabled = allowEdits,
            onClick = { onThemeSelected(ThemeMode.Dark) },
        )

        AppearanceSectionHeader(title = strings.appearance.typographySection)

        AppearanceTypographyCard(
            smallLabel = strings.appearance.typographySmallLabel,
            defaultLabel = strings.appearance.typographyDefaultLabel,
            largeLabel = strings.appearance.typographyLargeLabel,
            sliderValue = sliderValue,
            enabled = allowEdits,
            onValueChange = onTextScaleChange,
            onValueChangeFinished = onTextScaleChangeFinished,
            sampleText = strings.appearance.typographySample,
            sampleScale = sampleScale,
        )

        AppearanceSectionHeader(title = strings.appearance.accessibilitySection)

        AppearanceToggleCard(
            title = strings.appearance.reduceMotionTitle,
            subtitle = strings.appearance.reduceMotionSubtitle,
            checked = settings.reduceMotion,
            enabled = allowEdits,
            onCheckedChange = onToggleReduceMotion,
        )

        AppearanceToggleCard(
            title = strings.appearance.highContrastTitle,
            subtitle = strings.appearance.highContrastSubtitle,
            checked = settings.highContrast,
            enabled = allowEdits,
            onCheckedChange = onToggleHighContrast,
        )
    }
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
