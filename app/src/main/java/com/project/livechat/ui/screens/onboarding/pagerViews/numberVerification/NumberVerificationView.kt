package com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification

import android.app.Activity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.livechat.R
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors
import com.project.livechat.ui.theme.LiveChatTheme
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OneTimePasswordErrors
import com.project.livechat.ui.viewmodels.OnBoardingViewModel
import com.project.livechat.ui.widgets.dialog.ErrorAlertDialog
import com.project.livechat.ui.widgets.dialog.ProgressAlertDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingNumberVerification(onBoardingViewModel: OnBoardingViewModel) {
    val context = LocalContext.current
    val activity = context as Activity

    val state = onBoardingViewModel.screenState
    val oneTimePassStateUI =
        onBoardingViewModel.oneTimePassStateUI.collectAsStateWithLifecycle().value
    val showProgressDialog = remember { mutableStateOf(false) }
    val showErrorDialog = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = oneTimePassStateUI) {
        showProgressDialog.value = oneTimePassStateUI.isLoading()
        showErrorDialog.value = oneTimePassStateUI.isError()
        when (oneTimePassStateUI) {
            StateUI.Idle -> {

            }

            is StateUI.Success -> {

            }

            else -> Unit
        }
    }


    LaunchedEffect(key1 = context) {
        onBoardingViewModel.validationEvents.collect { event ->
            when (event) {
                is ValidationResult.Success -> {
                    showProgressDialog.value = true
                    onBoardingViewModel.callSmsVerification(
                        fullNum = state.fullNumber,
                        activity = activity,
                        callbacks = onBoardingViewModel.callbacks
                    )
                }

                is ValidationResult.Error -> {
                    val errorMessage = when (event.errorType) {
                        OnBoardingValidationErrors.INVALID_NUMBER ->
                            context.getString(R.string.on_boarding_invalid_number_error)
                    }
                    onBoardingViewModel.parseOnBoardingError(
                        message = errorMessage,
                        errorType = event.errorType
                    )
                }

                is ValidationResult.Idle -> Unit
            }
        }
    }

    if (showProgressDialog.value) {
        ProgressAlertDialog(
            title = "Enviando SMS",
            description = "Aguarde enquanto enviamos um SMS para que você possa confirmar seu número"
        )
    }

    if (showErrorDialog.value) {
        if (oneTimePassStateUI !is StateUI.Error) return

        val errorRes = when (oneTimePassStateUI.type as? OneTimePasswordErrors) {
            OneTimePasswordErrors.INVALID_CREDENTIALS -> R.string.app_name
            OneTimePasswordErrors.SMS_QUOTA_EXCEEDED -> R.string.app_name
            OneTimePasswordErrors.NULL_ACTIVITY -> R.string.app_name
            OneTimePasswordErrors.TIME_OUT -> R.string.app_name
            OneTimePasswordErrors.GENERIC_ERROR -> R.string.app_name
            null -> R.string.generic_error_message
        }

        ErrorAlertDialog(
            title = "Error",
            description = stringResource(id = errorRes),
            confirmAction = {
                onBoardingViewModel.callSmsVerification(
                    fullNum = state.fullNumber,
                    activity = activity,
                    callbacks = onBoardingViewModel.callbacks
                )
            })
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
                        onBoardingViewModel.onValidationEvent(
                            NumberVerificationFormEvent.PhoneCodeChanged(
                                it
                            )
                        )
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
                        onBoardingViewModel.onValidationEvent(
                            NumberVerificationFormEvent.PhoneNumberChanged(
                                it
                            )
                        )
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
                    onBoardingViewModel.onValidationEvent(NumberVerificationFormEvent.Submit)
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
    val onBoardingViewModel = hiltViewModel<OnBoardingViewModel>()
    LiveChatTheme {
        OnBoardingNumberVerification(onBoardingViewModel)
    }
}