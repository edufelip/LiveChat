package com.project.livechat.composeapp.ui.features.onboarding

data class CountryOption(
    val isoCode: String,
    val name: String,
    val dialCode: String,
    val flag: String,
) {
    companion object {
        val defaults =
            listOf(
                CountryOption("US", "United States", "+1", "US"),
                CountryOption("BR", "Brazil", "+55", "BR"),
                CountryOption("GB", "United Kingdom", "+44", "UK"),
                CountryOption("IN", "India", "+91", "IN"),
                CountryOption("CA", "Canada", "+1", "CA"),
            )

        fun default(): CountryOption = defaults.first()
    }
}

enum class OnboardingStep {
    PhoneEntry,
    OTP,
    Success,
}
