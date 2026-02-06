package com.edufelip.livechat.ui.util

actual fun phoneNumberFormattingService(): PhoneNumberFormattingService = IosPhoneNumberFormattingService()

private class IosPhoneNumberFormattingService : PhoneNumberFormattingService {
    override fun formatAsYouType(
        rawDigits: String,
        regionIso: String,
    ): String {
        if (rawDigits.isBlank()) return ""
        return formatAsYouTypeWithRegionalAdjustments(rawDigits, regionIso) {
            formatDefaultNational(rawDigits)
        }
    }

    override fun exampleNumber(regionIso: String): String? {
        val digits =
            when (regionIso.uppercase()) {
                "US", "CA" -> "6505553434"
                "BR" -> "21985670564"
                else -> "5550100"
            }
        return formatAsYouType(digits, regionIso)
    }
}

private fun formatDefaultNational(rawDigits: String): String {
    val digits = rawDigits.filter(Char::isDigit)
    if (digits.length <= 3) return digits
    if (digits.length <= 6) return "${digits.take(3)} ${digits.drop(3)}"
    val prefix = digits.take(3)
    val tail = digits.drop(3)
    return if (tail.length <= 4) {
        "$prefix $tail"
    } else {
        val split = tail.length - 4
        "$prefix ${tail.take(split)}-${tail.drop(split)}"
    }
}
