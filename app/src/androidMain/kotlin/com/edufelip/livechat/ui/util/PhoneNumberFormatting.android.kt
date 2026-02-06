package com.edufelip.livechat.ui.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

private val phoneNumberUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

actual fun phoneNumberFormattingService(): PhoneNumberFormattingService = AndroidPhoneNumberFormattingService()

private class AndroidPhoneNumberFormattingService : PhoneNumberFormattingService {
    override fun formatAsYouType(
        rawDigits: String,
        regionIso: String,
    ): String {
        if (rawDigits.isBlank()) return ""
        val formatter = phoneNumberUtil.getAsYouTypeFormatter(regionIso.uppercase())
        var formatted = ""
        rawDigits.forEach { digit ->
            formatted = formatter.inputDigit(digit)
        }
        return formatted
    }

    override fun exampleNumber(regionIso: String): String? {
        val region = regionIso.uppercase()
        return try {
            val example =
                listOf(
                    PhoneNumberType.MOBILE,
                    PhoneNumberType.FIXED_LINE_OR_MOBILE,
                    PhoneNumberType.FIXED_LINE,
                ).firstNotNullOfOrNull { type ->
                    phoneNumberUtil.getExampleNumberForType(region, type)
                } ?: return null
            phoneNumberUtil.format(example, PhoneNumberFormat.NATIONAL)
        } catch (_: Throwable) {
            null
        }
    }
}
