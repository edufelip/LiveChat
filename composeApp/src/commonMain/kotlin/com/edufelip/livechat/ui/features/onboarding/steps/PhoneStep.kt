package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.CountryOption
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PhoneStep(
    selectedCountry: CountryOption,
    phoneNumber: String,
    phoneError: String?,
    isLoading: Boolean,
    onPickCountry: () -> Unit,
    onPhoneChanged: (String) -> Unit,
    onContinue: () -> Unit,
    continueEnabled: Boolean = phoneNumber.isNotBlank() && !isLoading,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    val bringIntoViewRequester = BringIntoViewRequester()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val inputBounds = remember { mutableStateOf<Rect?>(null) }
    var isPhoneFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val contentBottomPadding =
        if (imeVisible) {
            0.dp
        } else {
            (MaterialTheme.spacing.xxxl + MaterialTheme.spacing.xxl) / 2
        }
    val cardShape = RoundedCornerShape(20.dp)
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val phoneBorderColor =
        if (isPhoneFocused) {
            MaterialTheme.colorScheme.primary
        } else {
            cardBorderColor
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.PHONE_STEP)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val bounds = inputBounds.value
                            if (bounds?.contains(down.position) != true) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .verticalScroll(scrollState)
                    .padding(horizontal = MaterialTheme.spacing.xl)
                    .padding(bottom = contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = strings.phoneTitle,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = strings.phoneSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.PHONE_COUNTRY_SELECTOR)
                            .clickable(onClick = onPickCountry),
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, cardBorderColor),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.lg),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                        ) {
                            Text(
                                text = strings.phoneCountryLabel.uppercase(),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                            ) {
                                Text(text = selectedCountry.flag, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = selectedCountry.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        Text(
                            text = selectedCountry.dialCode,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, phoneBorderColor),
                ) {
                    Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
                        Text(
                            text = strings.phoneFieldLabel.uppercase(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextField(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .testTag(OnboardingTestTags.PHONE_INPUT)
                                    .bringIntoViewRequester(bringIntoViewRequester)
                                    .onGloballyPositioned { coordinates ->
                                        inputBounds.value = coordinates.boundsInRoot()
                                    }
                                    .onFocusChanged { state ->
                                        isPhoneFocused = state.isFocused
                                        if (state.isFocused) {
                                            scope.launch {
                                                bringIntoViewRequester.bringIntoView()
                                            }
                                        }
                                    },
                            value = phoneNumber,
                            onValueChange = onPhoneChanged,
                            placeholder = { Text(strings.phoneFieldPlaceholder) },
                            singleLine = true,
                            isError = phoneError != null,
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = { focusManager.clearFocus() },
                                ),
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }

                if (phoneError != null) {
                    Text(
                        text = phoneError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(OnboardingTestTags.PHONE_ERROR),
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON),
                onClick = onContinue,
                enabled = continueEnabled,
                shape = RoundedCornerShape(percent = 50),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp).testTag(OnboardingTestTags.PHONE_LOADING_INDICATOR),
                    )
                } else {
                    Text(strings.continueCta)
                }
            }

            Text(
                text = strings.phoneTermsMessage,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
            )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun PhoneStepPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        PhoneStep(
            selectedCountry =
                CountryOption.default(
                    strings.onboarding.priorityCountryIsos,
                    strings.onboarding.defaultCountryIso,
                ),
            phoneNumber = "5550100",
            phoneError = null,
            isLoading = false,
            onPickCountry = {},
            onPhoneChanged = {},
            onContinue = {},
        )
    }
}
