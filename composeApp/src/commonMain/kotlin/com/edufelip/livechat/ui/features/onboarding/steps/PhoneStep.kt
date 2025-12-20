package com.edufelip.livechat.ui.features.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.onboarding.CountryOption
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.spacing
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
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings().onboarding
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.xl)
                .padding(
                    top = MaterialTheme.spacing.xxxl,
                    bottom = (MaterialTheme.spacing.xxxl + MaterialTheme.spacing.xxl) / 2,
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
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text(strings.phoneFieldLabel) },
                placeholder = { Text(strings.phoneFieldPlaceholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (phoneError != null) {
                        Text(text = phoneError, color = MaterialTheme.colorScheme.error)
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            onClick = onContinue,
            enabled = phoneNumber.isNotBlank() && !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text(strings.continueCta)
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
