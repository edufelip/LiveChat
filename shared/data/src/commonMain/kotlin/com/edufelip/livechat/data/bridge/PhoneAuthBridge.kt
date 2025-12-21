package com.edufelip.livechat.data.bridge

data class PhoneAuthBridgeError(
    val domain: String? = null,
    val code: Long? = null,
    val message: String? = null,
)

data class PhoneAuthBridgeResult(
    val verificationId: String? = null,
    val error: PhoneAuthBridgeError? = null,
)

interface PhoneAuthBridge {
    suspend fun sendCode(phoneE164: String): PhoneAuthBridgeResult

    suspend fun verifyCode(
        verificationId: String,
        code: String,
    ): PhoneAuthBridgeError?
}
