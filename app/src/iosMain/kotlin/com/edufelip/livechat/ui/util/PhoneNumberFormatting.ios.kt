package com.edufelip.livechat.ui.util

import PhoneNumberKit.PartialFormatter
import PhoneNumberKit.PhoneNumberFormat
import PhoneNumberKit.PhoneNumberKit

private val phoneNumberKit = PhoneNumberKit()

actual fun phoneNumberFormattingService(): PhoneNumberFormattingService = IosPhoneNumberFormattingService()

private class IosPhoneNumberFormattingService : PhoneNumberFormattingService {
    override fun formatAsYouType(
        rawDigits: String,
        regionIso: String,
    ): String {
        if (rawDigits.isBlank()) return ""
        return formatAsYouTypeWithRegionalAdjustments(rawDigits, regionIso) {
            val formatter =
                PartialFormatter(
                    phoneNumberKit = phoneNumberKit,
                    defaultRegion = regionIso.uppercase(),
                )
            formatter.formatPartial(rawDigits)
        }
    }

    override fun exampleNumber(regionIso: String): String? {
        val region = regionIso.uppercase()
        return try {
            val example = phoneNumberKit.getExampleNumberForCountry(region) ?: return null
            phoneNumberKit.format(example, toType = PhoneNumberFormat.NATIONAL)
        } catch (_: Throwable) {
            null
        }
    }
}
