package com.edufelip.livechat.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface PhoneNumberFormattingService {
    fun formatAsYouType(rawDigits: String, regionIso: String): String

    fun exampleNumber(regionIso: String): String?

    fun normalizeDigits(input: String): String = input.filter(Char::isDigit)
}

@Composable
fun rememberPhoneNumberFormattingService(): PhoneNumberFormattingService =
    remember { phoneNumberFormattingService() }

expect fun phoneNumberFormattingService(): PhoneNumberFormattingService
