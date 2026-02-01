package com.edufelip.livechat.ui.features.settings.appearance

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
import androidx.compose.ui.tooling.preview.Preview
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.AppearanceSettingsUiState
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.common.navigation.SettingsSubmenuBackHandler
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAppearanceSettingsPresenter
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AppearanceSettingsRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    targetItemId: String? = null,
) {
    val strings = liveChatStrings()
    if (LocalInspectionMode.current) {
        AppearanceSettingsScreen(
            state = previewState(),
            sliderValue = sliderFromScale(previewState().settings.textScale),
            sampleScale = previewState().settings.textScale,
            modifier = modifier,
            onBack = onBack,
            targetItemId = targetItemId,
        )
        return
    }

    val presenter = rememberAppearanceSettingsPresenter()
    val state by presenter.collectState()

    var sliderValue by remember { mutableStateOf(sliderFromScale(state.settings.textScale)) }
    var isDragging by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.settings.textScale) {
        if (!isDragging) {
            sliderValue = sliderFromScale(state.settings.textScale)
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

    val targetScale = scaleFromSlider(sliderValue)
    val sampleScale = if (state.settings.textScale > 0f) targetScale / state.settings.textScale else 1f

    // Enable back gesture support
    SettingsSubmenuBackHandler(
        enabled = true,
        onBack = onBack,
    )

    AppearanceSettingsScreen(
        state = state,
        sliderValue = sliderValue,
        sampleScale = sampleScale,
        modifier = modifier,
        onBack = onBack,
        targetItemId = targetItemId,
        onThemeSelected = { mode ->
            if (mode != state.settings.themeMode) {
                presenter.updateThemeMode(mode)
            }
        },
        onTextScaleChange = { value ->
            isDragging = true
            sliderValue = value
        },
        onTextScaleChangeFinished = {
            isDragging = false
            val newScale = targetScale.roundToTwoDecimals()
            if (!approximatelyEqual(newScale, state.settings.textScale)) {
                presenter.updateTextScale(newScale)
            }
        },
    )
}

private fun scaleFromSlider(value: Float): Float {
    val clamped = value.coerceIn(0f, 100f)
    val range = AppearanceSettings.MAX_TEXT_SCALE - AppearanceSettings.MIN_TEXT_SCALE
    return AppearanceSettings.MIN_TEXT_SCALE + (range * (clamped / 100f))
}

private fun sliderFromScale(scale: Float): Float {
    val clamped = scale.coerceIn(AppearanceSettings.MIN_TEXT_SCALE, AppearanceSettings.MAX_TEXT_SCALE)
    val range = AppearanceSettings.MAX_TEXT_SCALE - AppearanceSettings.MIN_TEXT_SCALE
    if (range == 0f) return 50f
    return ((clamped - AppearanceSettings.MIN_TEXT_SCALE) / range) * 100f
}

private fun Float.roundToTwoDecimals(): Float = (this * 100f).roundToInt() / 100f

private fun approximatelyEqual(
    a: Float,
    b: Float,
    epsilon: Float = 0.005f,
): Boolean = abs(a - b) <= epsilon

private fun previewState(): AppearanceSettingsUiState =
    AppearanceSettingsUiState(
        isLoading = false,
        settings = AppearanceSettings(themeMode = ThemeMode.System, textScale = 1.0f),
    )

@DevicePreviews
@Preview
@Composable
private fun AppearanceSettingsRoutePreview() {
    LiveChatPreviewContainer {
        AppearanceSettingsRoute()
    }
}
