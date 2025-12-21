package com.edufelip.livechat.data.auth.phone

import com.edufelip.livechat.data.bridge.PhoneAuthBridgeError
import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosPhoneAuthRepositoryTest {
    @Test
    fun mapsInvalidPhoneNumberErrors() {
        val error = PhoneAuthBridgeError(code = 17042L)
        assertEquals(PhoneAuthError.InvalidPhoneNumber, error.toPhoneAuthError())
    }

    @Test
    fun mapsInvalidVerificationCodeErrors() {
        val error = PhoneAuthBridgeError(code = 17044L)
        assertEquals(PhoneAuthError.InvalidVerificationCode, error.toPhoneAuthError())
    }

    @Test
    fun mapsQuotaAndNetworkErrors() {
        val quotaError = PhoneAuthBridgeError(code = 17052L)
        assertEquals(PhoneAuthError.TooManyRequests, quotaError.toPhoneAuthError())

        val networkError = PhoneAuthBridgeError(code = 17020L)
        assertEquals(PhoneAuthError.NetworkError, networkError.toPhoneAuthError())
    }

    @Test
    fun mapsNonFirebaseErrorsToUnknown() {
        val error = PhoneAuthBridgeError(domain = "test.domain", code = 1L, message = "oops")
        assertTrue(error.toPhoneAuthError() is PhoneAuthError.Unknown)
    }
}
