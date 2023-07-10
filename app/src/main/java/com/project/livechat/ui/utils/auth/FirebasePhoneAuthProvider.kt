package com.project.livechat.ui.utils.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.project.livechat.domain.providers.IPhoneAuthProvider
import com.project.livechat.domain.providers.IPhoneAuthProvider.Companion.PHONE_AUTH_TIMEOUT
import com.project.livechat.domain.utils.StateUI
import com.project.livechat.ui.screens.onboarding.pagerViews.oneTimePassword.OneTimePasswordErrors
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirebasePhoneAuthProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : IPhoneAuthProvider {
    override fun callSmsVerification(
        fullNum: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth.apply { useAppLanguage() })
            .setPhoneNumber(fullNum)
            .setTimeout(PHONE_AUTH_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun signInWithPhoneAuthCredential(
        verificationCode: String,
        oneTimePass: String
    ): Flow<StateUI<Unit>> {
        val credential = PhoneAuthProvider.getCredential(verificationCode, oneTimePass)
        return signInWithPhoneAuthCredential(credential)
    }

    override fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Flow<StateUI<Unit>> {
        return callbackFlow {
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        trySend(StateUI.Success(Unit))
                    } else {
                        if (it.exception is FirebaseAuthInvalidCredentialsException) {
                            trySend(
                                StateUI.Error(
                                    type = OneTimePasswordErrors.INVALID_CREDENTIALS,
                                    cause = it.exception
                                )
                            )
                        }
                        trySend(
                            StateUI.Error(
                                type = OneTimePasswordErrors.GENERIC_ERROR,
                                cause = it.exception
                            )
                        )
                    }
                }
            awaitClose()
        }.conflate()
    }
}