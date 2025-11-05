package com.project.livechat.domain.validation

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

private val phoneNumberUtil: PhoneNumberUtil by lazy {
    PhoneNumberUtil.getInstance()
}

internal actual fun isPhoneNumberValid(raw: String): Boolean {
    if (raw.isBlank()) return false
    return try {
        val parsed = phoneNumberUtil.parse(raw, null)
        phoneNumberUtil.isValidNumber(parsed)
    } catch (error: NumberParseException) {
        false
    } catch (error: IllegalArgumentException) {
        false
    }
}
