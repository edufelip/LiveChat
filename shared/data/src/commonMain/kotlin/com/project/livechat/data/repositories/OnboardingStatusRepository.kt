package com.project.livechat.data.repositories

import com.project.livechat.domain.repositories.IOnboardingStatusRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingStatusRepository(
    private val settings: Settings,
) : IOnboardingStatusRepository {
    private val state = MutableStateFlow(settings.getBoolean(ONBOARDING_COMPLETE_KEY, false))

    override val onboardingComplete: Flow<Boolean> = state.asStateFlow()

    override suspend fun setOnboardingComplete(complete: Boolean) {
        settings.putBoolean(ONBOARDING_COMPLETE_KEY, complete)
        state.value = complete
    }

    private companion object {
        const val ONBOARDING_COMPLETE_KEY = "onboarding.complete"
    }
}
