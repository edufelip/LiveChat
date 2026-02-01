package com.edufelip.livechat.data.auth.phone

import com.edufelip.livechat.data.bridge.PhoneAuthBridge
import com.edufelip.livechat.data.bridge.PhoneAuthBridgeError
import com.edufelip.livechat.data.util.isUiTestMode
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class IosPhoneAuthRepository(
    private val phoneAuthBridge: PhoneAuthBridge,
) : IPhoneAuthRepository {
    private var lastSession: PhoneVerificationSession? = null

    override fun requestVerification(
        phoneNumber: PhoneNumber,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> =
        flow {
            clearStoredSession()
            if (isUiTestMode()) {
                emit(PhoneAuthEvent.Loading)
                val session =
                    PhoneVerificationSession(
                        verificationId = UI_TEST_VERIFICATION_ID,
                        phoneNumber = phoneNumber,
                        createdAtMillis = currentEpochMillis(),
                    )
                lastSession = session
                emit(PhoneAuthEvent.CodeSent(session))
                return@flow
            }
            emit(PhoneAuthEvent.Loading)
            val result = phoneAuthBridge.sendCode(phoneNumber.e164)
            val error = result.error
            val verificationId = result.verificationId
            if (error != null) {
                emit(PhoneAuthEvent.Error(error.toPhoneAuthError()))
                return@flow
            }
            if (verificationId.isNullOrBlank()) {
                emit(PhoneAuthEvent.Error(PhoneAuthError.Unknown("Missing verification id")))
                return@flow
            }
            val session =
                PhoneVerificationSession(
                    verificationId = verificationId,
                    phoneNumber = phoneNumber,
                    createdAtMillis = currentEpochMillis(),
                )
            lastSession = session
            emit(PhoneAuthEvent.CodeSent(session))
        }

    override fun resendVerification(
        session: PhoneVerificationSession,
        presentationContext: PhoneAuthPresentationContext,
    ): Flow<PhoneAuthEvent> = requestVerification(session.phoneNumber, presentationContext)

    override suspend fun verifyCode(
        session: PhoneVerificationSession,
        code: String,
    ): PhoneAuthResult {
        if (isUiTestMode()) {
            return if (code == UI_TEST_VERIFICATION_CODE) {
                lastSession = session.markAutoVerified()
                PhoneAuthResult.Success
            } else {
                PhoneAuthResult.Failure(PhoneAuthError.InvalidVerificationCode)
            }
        }
        val error = phoneAuthBridge.verifyCode(session.verificationId, code)
        return if (error == null) {
            lastSession = session.markAutoVerified()
            PhoneAuthResult.Success
        } else {
            PhoneAuthResult.Failure(error.toPhoneAuthError())
        }
    }

    override fun clearActiveSession() {
        clearStoredSession()
    }

    private fun clearStoredSession() {
        lastSession = null
    }

    private companion object {
        const val UI_TEST_VERIFICATION_ID = "ui-test-verification-id"
        const val UI_TEST_VERIFICATION_CODE = "123123"
    }
}

internal fun PhoneAuthBridgeError.toPhoneAuthError(): PhoneAuthError =
    when (code) {
        17042L, // AuthErrorCode.invalidPhoneNumber
        17041L, // AuthErrorCode.missingPhoneNumber
        -> PhoneAuthError.InvalidPhoneNumber
        17044L, // AuthErrorCode.invalidVerificationCode
        17046L, // AuthErrorCode.invalidVerificationID
        17045L, // AuthErrorCode.missingVerificationID
        17043L, // AuthErrorCode.missingVerificationCode
        17004L, // AuthErrorCode.invalidCredential
        -> PhoneAuthError.InvalidVerificationCode
        17052L -> PhoneAuthError.TooManyRequests
        17029L -> PhoneAuthError.CodeExpired
        17020L -> PhoneAuthError.NetworkError
        else -> PhoneAuthError.Unknown(message.orEmpty())
    }
