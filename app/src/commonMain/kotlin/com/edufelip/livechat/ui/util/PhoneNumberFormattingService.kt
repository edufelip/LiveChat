package com.edufelip.livechat.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface PhoneNumberFormattingService {
    fun formatAsYouType(
        rawDigits: String,
        regionIso: String,
    ): String

    fun exampleNumber(regionIso: String): String?

    fun normalizeDigits(input: String): String = input.filter(Char::isDigit)
}

@Composable
fun rememberPhoneNumberFormattingService(): PhoneNumberFormattingService = remember { phoneNumberFormattingService() }

expect fun phoneNumberFormattingService(): PhoneNumberFormattingService

internal fun PhoneNumberFormattingService.formatAsYouTypeWithRegionalAdjustments(
    rawDigits: String,
    regionIso: String,
    baseFormatter: () -> String,
): String {
    val base = baseFormatter()
    return when (regionIso.uppercase()) {
        "BR" -> formatBrazilNationalAsYouType(rawDigits)
        else -> base
    }
}

private fun PhoneNumberFormattingService.formatBrazilNationalAsYouType(rawDigits: String): String {
    val digits = normalizeDigits(rawDigits)
    if (digits.isEmpty()) return ""

    val areaCode = digits.take(2)
    if (digits.length < 2) return "($areaCode"

    val subscriber = digits.drop(2)
    if (subscriber.isEmpty()) return "($areaCode)"
    if (subscriber.length <= 4) return "($areaCode) $subscriber"

    val splitIndex = subscriber.length - 4
    return "($areaCode) ${subscriber.take(splitIndex)}-${subscriber.drop(splitIndex)}"
}
