package com.project.livechat.composeapp.ui.features.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.ui.features.onboarding.CountryOption
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun PhoneStep(
    selectedCountry: CountryOption,
    phoneNumber: String,
    phoneError: String?,
    onPickCountry: () -> Unit,
    onPhoneChanged: (String) -> Unit,
    onContinue: () -> Unit,
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Enter your phone number",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "We'll send a verification code to confirm it's you.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPickCountry),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text("Phone number") },
                placeholder = { Text("Digits only") },
                singleLine = true,
                supportingText = {
                    if (phoneError != null) {
                        Text(text = phoneError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                },
            )
        }

        Button(
            onClick = onContinue,
            enabled = phoneNumber.isNotBlank(),
        ) {
            Text("Continue")
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
            onPickCountry = {},
            onPhoneChanged = {},
            onContinue = {},
        )
    }
}
