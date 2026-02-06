package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing

@Composable
internal fun OTPStep(
    otp: String,
    countdown: Int,
    canResend: Boolean,
    isRequesting: Boolean,
    isVerifying: Boolean,
    errorMessage: String?,
    onOtpChanged: (String) -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    verifyEnabled: Boolean = otp.length == OTP_DIGIT_COUNT && !isVerifying,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    val focusManager = LocalFocusManager.current
    var otpInputBounds by remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (otpInputBounds?.contains(down.position) != true) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                }.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .testTag(OnboardingTestTags.OTP_STEP)
                .padding(horizontal = MaterialTheme.spacing.xl)
                .padding(top = MaterialTheme.spacing.xxxl, bottom = MaterialTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Text(
                    text = strings.otpTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = strings.otpSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                OtpCodeField(
                    value = otp,
                    enabled = !isVerifying,
                    label = strings.otpFieldLabel,
                    onValueChange = onOtpChanged,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingTestTags.OTP_INPUT)
                            .onGloballyPositioned { coordinates ->
                                otpInputBounds = coordinates.boundsInRoot()
                            },
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(OnboardingTestTags.OTP_ERROR),
                        textAlign = TextAlign.Center,
                    )
                }
                if (canResend) {
                    TextButton(
                        onClick = onResend,
                        enabled = !isRequesting,
                        modifier = Modifier.testTag(OnboardingTestTags.OTP_RESEND_BUTTON),
                    ) {
                        Text(strings.resendCta)
                    }
                } else {
                    Text(
                        text = strings.resendCountdownLabel(countdown),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Button(
            onClick = onVerify,
            enabled = verifyEnabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag(OnboardingTestTags.OTP_VERIFY_BUTTON),
            shape = RoundedCornerShape(20.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp).testTag(OnboardingTestTags.OTP_VERIFY_LOADING),
                )
            } else {
                Text(strings.verifyCta)
            }
        }
    }
}

@Composable
private fun OtpCodeField(
    value: String,
    enabled: Boolean,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = OTP_DIGIT_COUNT,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input.filter(Char::isDigit).take(digitCount)
            onValueChange(filtered)
        },
        enabled = enabled,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .semantics { contentDescription = label }
                .clickable { focusRequester.requestFocus() },
        interactionSource = interactionSource,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                innerTextField()
                OtpDigitRow(
                    value = value,
                    isFocused = isFocused,
                    digitCount = digitCount,
                )
            }
        },
    )
}

@Composable
private fun OtpDigitRow(
    value: String,
    isFocused: Boolean,
    digitCount: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val cellSize = 52.dp
        val totalCellWidth = cellSize * digitCount
        val maxGap = (maxWidth - totalCellWidth) / (digitCount - 1)
        val spacing = minOf(maxGap, 24.dp).coerceAtLeast(0.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(digitCount) { index ->
                val char = value.getOrNull(index)?.toString().orEmpty()
                val isActive =
                    isFocused &&
                        (index == value.length || (value.length == digitCount && index == digitCount - 1))
                val borderColor =
                    if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                Surface(
                    modifier = Modifier.size(cellSize),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, borderColor),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun OtpStepPreview() {
    LiveChatPreviewContainer {
        OTPStep(
            otp = "123456",
            countdown = 24,
            canResend = false,
            isRequesting = false,
            isVerifying = false,
            errorMessage = null,
            onOtpChanged = {},
            onResend = {},
            onVerify = {},
        )
    }
}

private const val OTP_DIGIT_COUNT = 6
