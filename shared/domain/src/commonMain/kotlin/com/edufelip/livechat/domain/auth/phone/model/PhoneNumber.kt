package com.edufelip.livechat.domain.auth.phone.model

/**
 * Represents an E.164-compatible phone number composed of the dial code and the national digits.
 */
data class PhoneNumber(
    val dialCode: String,
    val nationalNumber: String,
) {
    init {
        require(dialCode.startsWith("+")) { "Dial code must include the '+' prefix" }
        require(nationalNumber.all(Char::isDigit)) { "National number must contain only digits" }
    }

    val e164: String = dialCode + nationalNumber
}
