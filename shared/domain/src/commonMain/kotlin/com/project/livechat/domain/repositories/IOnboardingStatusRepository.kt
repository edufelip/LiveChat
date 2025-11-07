package com.project.livechat.domain.repositories

import kotlinx.coroutines.flow.Flow

interface IOnboardingStatusRepository {
    val onboardingComplete: Flow<Boolean>

    suspend fun setOnboardingComplete(complete: Boolean)
}
