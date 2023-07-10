package com.project.livechat.domain.providers

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.project.livechat.domain.utils.StateUI
import kotlinx.coroutines.flow.Flow

interface IPhoneAuthProvider {
    fun callSmsVerification(
        fullNum: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    )

    fun signInWithPhoneAuthCredential(
        verificationCode: String,
        oneTimePass: String
    ): Flow<StateUI<Unit>>

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Flow<StateUI<Unit>>

    companion object {
        const val PHONE_AUTH_TIMEOUT = 60
        const val PHONE_VERIFY_MARGIN = 3
    }
}