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
import com.project.livechat.domain.providers.IPhoneAuthProvider.Companion.PHONE_AUTH_TIMEOUT
import com.project.livechat.domain.providers.IPhoneAuthProvider.Companion.PHONE_VERIFY_MARGIN
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.domain.validators.PhoneNumberValidator
import com.project.livechat.domain.validators.ValidationResult
import com.project.livechat.ui.ValidationViewModel
import com.project.livechat.ui.screens.onboarding.OnBoardingValidationErrors
import com.project.livechat.ui.screens.onboarding.models.NumberVerificationFormState
import com.project.livechat.ui.screens.onboarding.pagerViews.numberVerification.NumberVerificationFormEvent
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OneTimePasswordErrors
import com.project.livechat.ui.utils.coalesce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val phoneAuthProvider: IPhoneAuthProvider,
    private val phoneNumberValidator: PhoneNumberValidator
) : ValidationViewModel() {

    private val _sendSmsStateUI: MutableStateFlow<StateUI<Unit>> =
        MutableStateFlow(StateUI.Idle)
    val sendSmsStateUI = _sendSmsStateUI.asStateFlow()

    private val _verifyCodeStateUI: MutableStateFlow<StateUI<Unit>> =
        MutableStateFlow(StateUI.Idle)
    val verifyCodeStateUI = _verifyCodeStateUI.asStateFlow()

    private val _timeoutCount: MutableStateFlow<Int> = MutableStateFlow(PHONE_AUTH_TIMEOUT)
    val timeoutCount = _timeoutCount.asStateFlow()

    var screenState by mutableStateOf(NumberVerificationFormState())

    init {
        val epochSeconds = savedStateHandle.get<Int>(KEY_EPOCH_SECONDS)
        val timeout = savedStateHandle.get<Int>(KEY_TIMEOUT)
        coalesce(epochSeconds, timeout)?.let { (epochSeconds, timeout) ->
            val nowSeconds = System.currentTimeMillis().div(1000).toInt()
            val secDiff = nowSeconds - epochSeconds
            val result = timeout - secDiff - 1
            if (result >= PHONE_VERIFY_MARGIN) {
                startCountDown(result)
                savedStateHandle.get<NumberVerificationFormState>(KEY_SCREEN_STATE)?.let {
                    screenState = it
                }
            }
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

            is NumberVerificationFormEvent.Submit -> submitNumVerificationForm()
        }
    }

    fun navigateForward() {
        val value = screenState.currentPage
        val aimPage =
            if (value >= screenState.totalPages.minus(1)) screenState.totalPages.minus(1) else value + 1
        screenState = screenState.copy(currentPage = aimPage)
    }

    fun navigateBackwards() {
        val value = screenState.currentPage
        val aimPage =
            if (value <= 0) 0 else value - 1
        screenState = screenState.copy(currentPage = aimPage)
    }

    fun startCountDown(count: Int? = null) {
        val max = count ?: (PHONE_AUTH_TIMEOUT - 1)
        viewModelScope.launch {
            for (i in max downTo 0) {
                setStateHandleTimeOut(i, System.currentTimeMillis().div(1000).toInt())
                _timeoutCount.value = i
                delay(1000L)
            }
        }
    }

    fun parseOnBoardingError(message: String, errorType: OnBoardingValidationErrors) {
        when (errorType) {
            OnBoardingValidationErrors.INVALID_NUMBER -> screenState = screenState.copy(
                phoneNumError = message
            )
        }
    }

    fun callSmsVerification(
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        _sendSmsStateUI.value = StateUI.Loading
        try {
            phoneAuthProvider.callSmsVerification(
                fullNum = this.screenState.fullNumber,
                activity = activity,
                callbacks = callbacks
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            setStateHandleScreenState(screenState.copy(currentPage = screenState.currentPage + 1))
        }
    }

    private fun resetFormError() {
        viewModelScope.launch {
            validationEventChannel.send(ValidationResult.Idle)
            screenState = screenState.copy(
                phoneNumError = null
            )
        }
    }

    private fun submitNumVerificationForm() {
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

    fun submitCodeVerification() {
        viewModelScope.launch {
            phoneAuthProvider.signInWithPhoneAuthCredential(
                screenState.storedVerificationId,
                screenState.oneTimePass
            ).onStart {
                _verifyCodeStateUI.value = StateUI.Loading
            }.catch {
                _verifyCodeStateUI.value = StateUI.Error()
            }.collect {
                _verifyCodeStateUI.value = it
            }
        }
    }

    private fun submitCodeVerification(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            phoneAuthProvider.signInWithPhoneAuthCredential(
                credential
            ).onStart {
                _verifyCodeStateUI.value = StateUI.Loading
            }.catch {
                _verifyCodeStateUI.value = StateUI.Error()
            }.collect {
                _verifyCodeStateUI.value = it
            }
        }
    }

    private fun setStateHandleScreenState(state: NumberVerificationFormState?) {
        savedStateHandle[KEY_SCREEN_STATE] = state
    }

    private fun setStateHandleTimeOut(seconds: Int, currentTimeMillis: Int) {
        savedStateHandle[KEY_TIMEOUT] = seconds
        savedStateHandle[KEY_EPOCH_SECONDS] = currentTimeMillis
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//            submitCodeVerification(credential) Todo (don't enable this yet)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val errorType: OneTimePasswordErrors = when (e) {
                is FirebaseAuthInvalidCredentialsException -> OneTimePasswordErrors.INVALID_CREDENTIALS
                is FirebaseTooManyRequestsException -> OneTimePasswordErrors.SMS_QUOTA_EXCEEDED
                is FirebaseAuthMissingActivityForRecaptchaException -> OneTimePasswordErrors.NULL_ACTIVITY
                else -> OneTimePasswordErrors.GENERIC_ERROR
            }
            setStateHandleScreenState(null)
            _sendSmsStateUI.value = StateUI.Error(errorType, e)
            _verifyCodeStateUI.value = StateUI.Error(errorType, e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            screenState = screenState.copy(storedVerificationId = verificationId, token = token)
            _sendSmsStateUI.value = StateUI.Success(Unit)
            startCountDown()
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            setStateHandleScreenState(null)
            _sendSmsStateUI.value = StateUI.Error(OneTimePasswordErrors.TIME_OUT, Exception(p0))
        }
    }

    companion object {
        const val KEY_SCREEN_STATE = "saved_state_screen"
        const val KEY_TIMEOUT = "saved_state_timeout"
        const val KEY_EPOCH_SECONDS = "saved_state_epoch_seconds"
    }
}