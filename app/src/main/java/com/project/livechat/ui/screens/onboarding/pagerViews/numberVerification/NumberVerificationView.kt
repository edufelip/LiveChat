package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.theme.LiveChatTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingNumberVerification(onBoardingViewModel: OnBoardingViewModel) {

    val context = LocalContext.current
    val state = onBoardingViewModel.state
    LaunchedEffect(key1 = context) {
        onBoardingViewModel.validationEvents.collect { event ->
            when(event) {
                is ValidationResult.Success -> {
                    onBoardingViewModel.navigateForward()
                }
                is ValidationResult.Error -> {
                    val errorType = event.errorType
                    onBoardingViewModel.updateErrorText(context.getString(errorType.messageResourceId), errorType)
                }
                is ValidationResult.Idle -> Unit
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(all = 24.dp)
        ) {
            Text(text = "Enter your phone number")
            Text(text = "LiveChat will send you a SMS message to verify your phone number. Enter your country code and phone number")
            Row(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedTextField(
                    value = state.phoneCode,
                    onValueChange = {
                        if (it.length > 3) return@OutlinedTextField
                        onBoardingViewModel.onEvent(NumberVerificationFormEvent.PhoneCodeChanged(it))
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
                        onBoardingViewModel.onEvent(NumberVerificationFormEvent.PhoneNumberChanged(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    maxLines = 1
                )
            }
            if (state.phoneError != null) {
                Text(
                    text = state.phoneError,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    onBoardingViewModel.onEvent(NumberVerificationFormEvent.Submit)
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
    val onBoardingViewModel = viewModel<OnBoardingViewModel>()
    LiveChatTheme {
        OnBoardingNumberVerification(onBoardingViewModel)
    }
}