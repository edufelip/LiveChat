package com.project.livechat.ui.screens.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.project.livechat.domain.validators.ValidatePhoneNumber
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.ValidationViewModel
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val validatePhoneNumber: ValidatePhoneNumber = ValidatePhoneNumber()
) : ValidationViewModel() {

    var state by mutableStateOf(NumberVerificationFormState())
    val pages = 3.apply { this.minus(1) }

    fun onEvent(event: NumberVerificationFormEvent) {
        when (event) {
            is NumberVerificationFormEvent.PhoneNumberChanged -> {
                state = state.copy(phoneNum = event.number)
                resetFormError()
            }

            is NumberVerificationFormEvent.PhoneCodeChanged -> {
                state = state.copy(phoneCode = event.code)
                resetFormError()
            }

            is NumberVerificationFormEvent.Submit -> submitData()
        }
    }

    fun navigateForward() {
        val value = state.currentPage
        val aimPage =
            if (value >= pages) pages else value + 1
        state = state.copy(currentPage = aimPage)
    }

    fun navigateBackwards() {
        val value = state.currentPage
        val aimPage =
            if (value <= 0) 0 else value - 1
        state = state.copy(currentPage = aimPage)
    }

    private fun resetFormError() {
        viewModelScope.launch {
            validationEventChannel.send(ValidationResult.Idle)
            state = state.copy(
                phoneError = null
            )
        }
    }

    fun updateErrorText(message: String, errorType: OnBoardingErrors) {
        when (errorType) {
            OnBoardingErrors.INVALID_NUMBER -> state = state.copy(
                phoneError = message
            )
        }
    }

    private fun submitData() {
        val completePhoneNumber = "${state.phoneCode}${state.phoneNum}"
        val numberValidationResult = validatePhoneNumber(completePhoneNumber)
        if (numberValidationResult is ValidationResult.Error) {
            viewModelScope.launch {
                validationEventChannel.send(numberValidationResult)
            }
            return
        }

        // Make call to send sms
        viewModelScope.launch {
            validationEventChannel.send(ValidationResult.Success)
        }
    }
}