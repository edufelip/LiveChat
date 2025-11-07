package com.project.livechat.composeapp.ui.features.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.resources.liveChatStrings
import com.project.livechat.composeapp.ui.theme.spacing
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
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    Column(
        modifier =
            modifier
                .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth(),
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (canResend) {
                TextButton(
                    onClick = onResend,
                    enabled = !isRequesting,
                    modifier = Modifier.heightIn(min = 48.dp),
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

        Button(
            onClick = onVerify,
            enabled = otp.length == 6 && !isVerifying,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            if (isVerifying) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text(strings.verifyCta)
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
