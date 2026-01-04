package com.edufelip.livechat.domain.utils

import com.edufelip.livechat.domain.auth.phone.model.PhoneNumber

/**
 * Removes any non-digit characters so phone numbers like "+5511987654321" and "5511987654321"
 * can be compared reliably.
 */
fun normalizePhoneNumber(phoneNumber: String): String = phoneNumber.filter { it.isDigit() }

/**
 * Ensures the number contains a leading '+' when digits are available. If the contact was saved
 * without a country code we best-effort prepend the calling code derived from [regionIso].
 */
fun canonicalPhoneNumber(
    phoneNumber: String,
    regionIso: String? = null,
): String {
    val trimmed = phoneNumber.trim()
    val digits = normalizePhoneNumber(trimmed)
    if (digits.isEmpty()) return trimmed
    if (trimmed.startsWith("+")) return "+$digits"

    val callingCode = countryCallingCodeFor(regionIso)
    val digitsWithCode =
        when {
            callingCode.isNullOrBlank() -> digits
            digits.startsWith(callingCode) -> digits
            else -> callingCode + digits
        }
    return "+$digitsWithCode"
}

fun phoneNumberFromE164(phoneNumber: String): PhoneNumber? {
    val parts = splitE164PhoneNumber(phoneNumber) ?: return null
    return PhoneNumber(dialCode = parts.first, nationalNumber = parts.second)
}
