package com.edufelip.livechat.data.auth.phone

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebasePhoneAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : IPhoneAuthRepository {
    private var lastSession: PhoneVerificationSession? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun requestVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> {
        clearStoredSession()
        return launchVerification(phoneNumber, presentationContext, forceResend = false)
    }

    override fun resendVerification(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> {
        val token = resendToken
        return if (token == null) {
            callbackFlow {
                trySend(PhoneAuthEvent.Error(PhoneAuthError.ResendNotAvailable))
                close()
            }
        } else {
            launchVerification(session.phoneNumber, presentationContext, forceResend = true)
        }
    }

    override suspend fun verifyCode(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult =
        runCatching {
            val credential = PhoneAuthProvider.getCredential(session.verificationId, code)
            firebaseAuth.signInWithCredential(credential).await()
            lastSession = session.markAutoVerified()
            PhoneAuthResult.Success
        }.getOrElse { throwable ->
            PhoneAuthResult.Failure(throwable.toPhoneAuthError())
        }

    override fun clearActiveSession() {
        clearStoredSession()
    }

    private fun launchVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
        forceResend: Boolean,
    ): Flow<PhoneAuthEvent> =
        callbackFlow {
            trySend(PhoneAuthEvent.Loading)

            val callbacks =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        val session = lastSession ?: PhoneVerificationSession("", phoneNumber)
                        firebaseAuth
                            .signInWithCredential(credential)
                            .addOnSuccessListener {
                                val updated = session.markAutoVerified()
                                lastSession = updated
                                trySend(PhoneAuthEvent.VerificationCompleted(updated))
                            }.addOnFailureListener { error ->
                                trySend(PhoneAuthEvent.Error(error.toPhoneAuthError()))
                            }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        trySend(PhoneAuthEvent.Error(e.toPhoneAuthError()))
                        close()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        val session =
                            PhoneVerificationSession(
                                verificationId = verificationId,
                                phoneNumber = phoneNumber,
                                createdAtMillis = currentEpochMillis(),
                            )
                        lastSession = session
                        resendToken = token
                        trySend(PhoneAuthEvent.CodeSent(session))
                    }
                }

            val optionsBuilder =
                PhoneAuthOptions
                    .newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber.e164)
                    .setTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .setActivity(presentationContext.activity)
                    .setCallbacks(callbacks)

            if (forceResend) {
                resendToken?.let { optionsBuilder.setForceResendingToken(it) } ?: run {
                    trySend(PhoneAuthEvent.Error(PhoneAuthError.ResendNotAvailable))
                    close()
                    return@callbackFlow
                }
            }

            runCatching {
                PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            }.onFailure { throwable ->
                val error = throwable.toPhoneAuthError()
                trySend(PhoneAuthEvent.Error(error))
                close()
                return@callbackFlow
            }

            awaitClose { }
        }

    private fun clearStoredSession() {
        lastSession = null
        resendToken = null
    }

    private fun Throwable.toPhoneAuthError(): PhoneAuthError =
        when (this) {
            is FirebaseAuthInvalidCredentialsException -> PhoneAuthError.InvalidVerificationCode
            is FirebaseTooManyRequestsException -> PhoneAuthError.TooManyRequests
            is FirebaseAuthInvalidUserException -> PhoneAuthError.InvalidPhoneNumber
            is FirebaseException -> {
                val lower = message?.lowercase()
                if (lower?.contains("network") == true) {
                    PhoneAuthError.NetworkError
                } else if (lower?.contains("billing_not_enabled") == true) {
                    PhoneAuthError.Configuration(message)
                } else {
                    PhoneAuthError.Unknown(message)
                }
            }
            else -> PhoneAuthError.Unknown(message)
        }

    private companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 60L
    }
}
