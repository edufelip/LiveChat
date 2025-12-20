@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.data.auth.phone

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthCredential
import cocoapods.FirebaseAuth.FIRAuthErrorDomain
import cocoapods.FirebaseAuth.FIRPhoneAuthProvider
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume

class FirebasePhoneAuthRepository : IPhoneAuthRepository {
    private val auth: FIRAuth = FIRAuth.auth()
    private val phoneAuthProvider: FIRPhoneAuthProvider = FIRPhoneAuthProvider.providerWithAuth(auth)
    private var lastSession: PhoneVerificationSession? = null

    override fun requestVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> {
        clearStoredSession()
        return launchVerification(phoneNumber)
    }

    override fun resendVerification(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = launchVerification(session.phoneNumber)

    override suspend fun verifyCode(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult {
        val credential = phoneAuthProvider.credentialWithVerificationID(session.verificationId, code)
        return signInWithCredential(credential).also { result ->
            if (result is PhoneAuthResult.Success) {
                lastSession = session.markAutoVerified()
            }
        }
    }

    override fun clearActiveSession() {
        clearStoredSession()
    }

    private fun launchVerification(
        phoneNumber: PhoneNumber,
    ): Flow<PhoneAuthEvent> =
        callbackFlow {
            trySend(PhoneAuthEvent.Loading)

            phoneAuthProvider.verifyPhoneNumber(
                phoneNumber.e164,
                null,
            ) { verificationId, error ->
                if (error != null) {
                    trySend(PhoneAuthEvent.Error(error.toPhoneAuthError()))
                    close()
                    return@verifyPhoneNumber
                }

                if (verificationId.isNullOrBlank()) {
                    trySend(PhoneAuthEvent.Error(PhoneAuthError.Unknown("Missing verification id")))
                    close()
                    return@verifyPhoneNumber
                }

                val session =
                    PhoneVerificationSession(
                        verificationId = verificationId,
                        phoneNumber = phoneNumber,
                        createdAtMillis = currentEpochMillis(),
                    )
                lastSession = session
                trySend(PhoneAuthEvent.CodeSent(session))
                close()
            }

            awaitClose { }
        }

    private suspend fun signInWithCredential(
        credential: FIRAuthCredential,
    ): PhoneAuthResult =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithCredential(credential) { _, error ->
                val result =
                    if (error == null) {
                        PhoneAuthResult.Success
                    } else {
                        PhoneAuthResult.Failure(error.toPhoneAuthError())
                    }
                continuation.resume(result)
            }
        }

    private fun clearStoredSession() {
        lastSession = null
    }

    private fun NSError.toPhoneAuthError(): PhoneAuthError {
        if (domain != FIRAuthErrorDomain) {
            return PhoneAuthError.Unknown(localizedDescription)
        }
        return when (code.toLong()) {
            17042L, // AuthErrorCode.invalidPhoneNumber
            17041L, // AuthErrorCode.missingPhoneNumber
            -> PhoneAuthError.InvalidPhoneNumber
            17044L, // AuthErrorCode.invalidVerificationCode
            17046L, // AuthErrorCode.invalidVerificationID
            17045L, // AuthErrorCode.missingVerificationID
            17043L, // AuthErrorCode.missingVerificationCode
            17004L, // AuthErrorCode.invalidCredential
            -> PhoneAuthError.InvalidVerificationCode
            17052L, // AuthErrorCode.quotaExceeded
            -> PhoneAuthError.TooManyRequests
            17029L, // AuthErrorCode.expiredActionCode
            -> PhoneAuthError.CodeExpired
            17020L, // AuthErrorCode.networkError
            -> PhoneAuthError.NetworkError
            else -> PhoneAuthError.Unknown(localizedDescription)
        }
    }
}
