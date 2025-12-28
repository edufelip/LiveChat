package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val contentBottomPadding =
        if (imeVisible) {
            0.dp
        } else {
            (MaterialTheme.spacing.xxxl + MaterialTheme.spacing.xxl) / 2
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = MaterialTheme.spacing.xl)
                    .padding(
                        top = MaterialTheme.spacing.xxxl,
                        bottom = contentBottomPadding,
                    ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxl, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Text(
                    text = strings.phoneTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = strings.phoneSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.PHONE_COUNTRY_SELECTOR)
                            .clickable(onClick = onPickCountry),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(MaterialTheme.spacing.md)) {
                        Text(
                            text = selectedCountry.flag + " " + selectedCountry.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = selectedCountry.dialCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                OutlinedTextField(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.PHONE_INPUT)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onGloballyPositioned { coordinates ->
                                inputBounds.value = coordinates.boundsInRoot()
                            }
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    scope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                    value = phoneNumber,
                    onValueChange = onPhoneChanged,
                    label = { Text(strings.phoneFieldLabel) },
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
                )
                if (phoneError != null) {
                    Text(
                        text = phoneError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(OnboardingTestTags.PHONE_ERROR),
                    )
                }
            }

            Spacer(modifier = Modifier.heightIn(min = MaterialTheme.spacing.xl))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag(OnboardingTestTags.PHONE_CONTINUE_BUTTON)
                        .clickable(enabled = continueEnabled, onClick = onContinue)
                        .semantics {
                            role = Role.Button
                            if (!continueEnabled) {
                                disabled()
                            }
                            onClick {
                                if (continueEnabled) {
                                    onContinue()
                                    true
                                } else {
                                    false
                                }
                            }
                        },
            ) {
                Button(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                    onClick = onContinue,
                    enabled = continueEnabled,
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
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun PhoneStepPreview() {
    LiveChatPreviewContainer {
        PhoneStep(
            selectedCountry = CountryOption.default(),
            phoneNumber = "5550100",
            phoneError = null,
            isLoading = false,
            onPickCountry = {},
            onPhoneChanged = {},
            onContinue = {},
        )
    }
}
