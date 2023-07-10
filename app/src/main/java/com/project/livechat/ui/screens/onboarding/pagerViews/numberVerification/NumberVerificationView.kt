package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.ui.viewmodels.OnBoardingViewModel


@Composable
fun OnBoardingNumberVerification(onBoardingViewModel: OnBoardingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activity = context as Activity
    val state = onBoardingViewModel.screenState

    NumberVerificationContent(
        state = state,
        validationPhoneCodeChanged = { text ->
            onBoardingViewModel.onValidationEvent(
                NumberVerificationFormEvent.PhoneCodeChanged(
                    text
                )
            )
        },
        validationPhoneNumberChanged = { text ->
            onBoardingViewModel.onValidationEvent(
                NumberVerificationFormEvent.PhoneNumberChanged(
                    text
                )
            )
        },
        validationSubmit = {
            onBoardingViewModel.onValidationEvent(NumberVerificationFormEvent.Submit)
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberVerificationContent(
    state: NumberVerificationFormState,
    validationPhoneCodeChanged: (text: String) -> Unit,
    validationPhoneNumberChanged: (text: String) -> Unit,
    validationSubmit: () -> Unit,
) {
    Scaffold {
        Column(
            modifier = Modifier.padding(all = 24.dp)
        ) {
            Text(
                text = "Enter your phone number",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LiveChat will send you a SMS message to verify your phone number. Enter your country code and phone number",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedTextField(
                    value = state.phoneCode,
                    onValueChange = {
                        if (it.length > 3) return@OutlinedTextField
                        validationPhoneCodeChanged.invoke(it)
                    },
                    modifier = Modifier.width(98.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Text(
                            text = "+",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp, end = 0.dp)
                        )
                    },
                    maxLines = 1
                )
                OutlinedTextField(
                    value = state.phoneNum,
                    onValueChange = {
                        validationPhoneNumberChanged.invoke(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    maxLines = 1
                )
            }
            if (state.phoneNumError != null) {
                Text(
                    text = state.phoneNumError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    validationSubmit.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(text = "Continue")
            }
        }
    }
}

@Preview
@Composable
fun OnBoardingNumberVerificationPreview() {
    LiveChatTheme {
        NumberVerificationContent(
            state = NumberVerificationFormState(),
            validationPhoneCodeChanged = {},
            validationPhoneNumberChanged = {},
            validationSubmit = {}
        )
    }
}