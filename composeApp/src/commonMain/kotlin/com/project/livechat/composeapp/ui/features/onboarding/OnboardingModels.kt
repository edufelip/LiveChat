package com.project.livechat.composeapp.ui.features.onboarding

import com.project.livechat.composeapp.ui.features.onboarding.generated.GeneratedCountryOptions

data class CountryOption(
    val isoCode: String,
    val name: String,
    val dialCode: String,
    val flag: String,
) {
    companion object {
        private val priorityIsoCodes = listOf("US", "CA", "BR")
        private val countries: List<CountryOption> =
            GeneratedCountryOptions.sortedWith(
                compareBy<CountryOption> {
                    val normalizedIso = it.isoCode.uppercase()
                    val index = priorityIsoCodes.indexOf(normalizedIso)
                    if (index >= 0) index else Int.MAX_VALUE
                }.thenBy { it.name },
            )

        val defaults: List<CountryOption>
            get() = countries

        fun default(): CountryOption =
            countries.firstOrNull { it.isoCode.equals("US", ignoreCase = true) }
                ?: countries.first()

        fun fromIsoCode(isoCode: String): CountryOption =
            countries.firstOrNull { it.isoCode.equals(isoCode, ignoreCase = true) }
                ?: default()
    }
}

enum class OnboardingStep {
    PhoneEntry,
    OTP,
    Success,
}
