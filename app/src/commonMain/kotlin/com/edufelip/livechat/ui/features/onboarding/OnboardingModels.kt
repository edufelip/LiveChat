package com.edufelip.livechat.ui.features.onboarding

import com.edufelip.livechat.ui.features.onboarding.generated.GeneratedCountryOptions

data class CountryOption(
    val isoCode: String,
    val name: String,
    val dialCode: String,
    val flag: String,
) {
    companion object {
        private fun sortedCountries(priorityIsoCodes: List<String>): List<CountryOption> {
            val normalizedPriority =
                priorityIsoCodes.mapNotNull { code ->
                    code.trim().takeIf { it.isNotEmpty() }?.uppercase()
                }
            val indexMap = normalizedPriority.withIndex().associate { it.value to it.index }
            return GeneratedCountryOptions.sortedWith(
                compareBy<CountryOption> {
                    val normalizedIso = it.isoCode.uppercase()
                    indexMap[normalizedIso] ?: Int.MAX_VALUE
                }.thenBy { it.name },
            )
        }

        fun defaults(priorityIsoCodes: List<String>): List<CountryOption> = sortedCountries(priorityIsoCodes)

        fun default(
            priorityIsoCodes: List<String>,
            defaultIsoCode: String,
        ): CountryOption {
            val countries = sortedCountries(priorityIsoCodes)
            val normalizedDefault = defaultIsoCode.trim()
            return countries.firstOrNull { it.isoCode.equals(normalizedDefault, ignoreCase = true) }
                ?: countries.first()
        }

        fun fromIsoCode(
            isoCode: String,
            priorityIsoCodes: List<String>,
            defaultIsoCode: String,
        ): CountryOption {
            val countries = sortedCountries(priorityIsoCodes)
            return countries.firstOrNull { it.isoCode.equals(isoCode, ignoreCase = true) }
                ?: default(priorityIsoCodes, defaultIsoCode)
        }
    }
}

enum class OnboardingStep {
    PhoneEntry,
    OTP,
    Success,
}
