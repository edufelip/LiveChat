package com.edufelip.livechat.domain.utils

interface PhoneNumberFormatter {
    fun normalize(phoneNumber: String): String
}

class DefaultPhoneNumberFormatter : PhoneNumberFormatter {
    override fun normalize(phoneNumber: String): String = normalizePhoneNumber(phoneNumber)
}
