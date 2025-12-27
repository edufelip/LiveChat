package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.OnboardingTestTags
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    verifyEnabled: Boolean = otp.length == 6 && !isVerifying,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(OnboardingTestTags.OTP_STEP)
                .padding(horizontal = MaterialTheme.spacing.xl, vertical = MaterialTheme.spacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxl, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
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

        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = otp,
                onValueChange = onOtpChanged,
                label = { Text(strings.otpFieldLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(OnboardingTestTags.OTP_INPUT),
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(OnboardingTestTags.OTP_ERROR),
                )
            }
            if (canResend) {
                TextButton(
                    onClick = onResend,
                    enabled = !isRequesting,
                    modifier =
                        Modifier
                            .heightIn(min = 48.dp)
                            .testTag(OnboardingTestTags.OTP_RESEND_BUTTON),
                ) {
                    Text(strings.resendCta)
                }
            } else {
                Text(
                    text = strings.resendCountdownLabel(countdown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .heightIn(min = 48.dp)
                    .testTag(OnboardingTestTags.OTP_VERIFY_BUTTON)
                    .clickable(enabled = verifyEnabled, onClick = onVerify)
                    .semantics {
                        role = Role.Button
                        if (!verifyEnabled) {
                            disabled()
                        }
                        onClick {
                            if (verifyEnabled) {
                                onVerify()
                                true
                            } else {
                                false
                            }
                        }
                    },
        ) {
            Button(
                onClick = onVerify,
                enabled = verifyEnabled,
                modifier = Modifier.heightIn(min = 48.dp),
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
