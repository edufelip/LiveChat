package com.project.livechat.ui.viewmodels

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.project.livechat.domain.providers.IPhoneAuthProvider
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.domain.validators.PhoneNumberValidator
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.ValidationViewModel
import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OneTimePasswordErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val phoneAuthProvider: IPhoneAuthProvider
) : ValidationViewModel() {

    private val _oneTimePassStateUI: MutableStateFlow<StateUI<String>> =
        MutableStateFlow(StateUI.Idle)
    val oneTimePassStateUI = _oneTimePassStateUI.asStateFlow()
    val phoneNumberValidator: PhoneNumberValidator = PhoneNumberValidator()
    var screenState by mutableStateOf(NumberVerificationFormState())

    init {
        savedStateHandle.get<Boolean>(KEY_VERIFICATION_ON)?.let {
            // TODO (set state = verification happening / restore state or ask for user to wait)
            // TODO (clear flag when verification ends)
        }
    }

    fun onValidationEvent(event: NumberVerificationFormEvent) {
        when (event) {
            is NumberVerificationFormEvent.PhoneNumberChanged -> {
                screenState = screenState.copy(phoneNum = event.number)
                resetFormError()
            }

            is NumberVerificationFormEvent.PhoneCodeChanged -> {
                screenState = screenState.copy(phoneCode = event.code)
                resetFormError()
            }

            is NumberVerificationFormEvent.OneTimePassChanged -> {
                screenState = screenState.copy(oneTimePass = event.password)
            }

            is NumberVerificationFormEvent.Submit -> submitData()
        }
    }

    fun navigateForward() {
        val value = screenState.currentPage
        val aimPage =
            if (value >= screenState.totalPages) screenState.totalPages else value + 1
        screenState = screenState.copy(currentPage = aimPage)
    }

    fun navigateBackwards() {
        val value = screenState.currentPage
        val aimPage =
            if (value <= 0) 0 else value - 1
        screenState = screenState.copy(currentPage = aimPage)
    }

    private fun resetFormError() {
        viewModelScope.launch {
            validationEventChannel.send(ValidationResult.Idle)
            screenState = screenState.copy(
                phoneError = null
            )
        }
    }

    fun parseOnBoardingError(message: String, errorType: OnBoardingValidationErrors) {
        when (errorType) {
            OnBoardingValidationErrors.INVALID_NUMBER -> screenState = screenState.copy(
                phoneError = message
            )
        }
    }

    fun callSmsVerification(
        fullNum: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        try {
            phoneAuthProvider.callSmsVerification(
                fullNum = fullNum,
                activity = activity,
                callbacks = callbacks
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            savedStateHandle[KEY_VERIFICATION_ON] = true
        }
    }

    private fun submitData() {
        val completePhoneNumber = screenState.fullNumber
        val numberValidationResult = phoneNumberValidator(completePhoneNumber)

        if (numberValidationResult is ValidationResult.Error) {
            viewModelScope.launch {
                validationEventChannel.send(numberValidationResult)
            }
            return
        }

        viewModelScope.launch {
            validationEventChannel.send(ValidationResult.Success)
        }
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            _oneTimePassStateUI.value = StateUI.Success(credential.toString())
//            firebasePhoneAuthProvider.signInWithPhoneAuthCredential(credential) Todo (Don't use this step yet)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val errorType: OneTimePasswordErrors = when (e) {
                is FirebaseAuthInvalidCredentialsException -> OneTimePasswordErrors.INVALID_CREDENTIALS
                is FirebaseTooManyRequestsException -> OneTimePasswordErrors.SMS_QUOTA_EXCEEDED
                is FirebaseAuthMissingActivityForRecaptchaException -> OneTimePasswordErrors.NULL_ACTIVITY
                else -> OneTimePasswordErrors.GENERIC_ERROR
            }

            _oneTimePassStateUI.value = StateUI.Error(errorType, e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            screenState = screenState.copy(storedVerificationId = verificationId, token = token)
            navigateForward()
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            _oneTimePassStateUI.value = StateUI.Error(OneTimePasswordErrors.TIME_OUT, Exception(p0))
        }
    }

    companion object {
        const val KEY_VERIFICATION_ON = "saved_state_is_verification_on"
    }
}