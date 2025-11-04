package com.project.livechat.composeapp.ui.features.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OTPStep(
    otp: String,
    countdown: Int,
    timerActive: Boolean,
    onOtpChanged: (String) -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Enter the 6-digit code",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "We just sent a verification code to your number.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = otp,
                onValueChange = onOtpChanged,
                label = { Text("Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (timerActive) {
                Text(
                    text = "Resend available in ${countdown.coerceAtLeast(0)}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                TextButton(onClick = onResend) {
                    Text("Resend code")
                }
            }
        }

        Button(
            onClick = onVerify,
            enabled = otp.length == 6,
        ) {
            Text("Verify")
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
            timerActive = true,
            onOtpChanged = {},
            onResend = {},
            onVerify = {},
        )
    }
}
