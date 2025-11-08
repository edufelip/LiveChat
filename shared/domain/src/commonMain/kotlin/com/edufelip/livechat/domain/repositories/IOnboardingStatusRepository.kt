package com.edufelip.livechat.domain.repositories

import kotlinx.coroutines.flow.Flow

interface IOnboardingStatusRepository {
    val onboardingComplete: Flow<Boolean>

    suspend fun setOnboardingComplete(complete: Boolean)

    fun currentStatus(): Boolean
}
