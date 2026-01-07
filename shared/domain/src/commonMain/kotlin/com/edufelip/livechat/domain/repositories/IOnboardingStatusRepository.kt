package com.edufelip.livechat.domain.repositories

import kotlinx.coroutines.flow.Flow

interface IOnboardingStatusRepository {
    val onboardingComplete: Flow<Boolean>
    val welcomeSeen: Flow<Boolean>

    suspend fun setOnboardingComplete(complete: Boolean)

    suspend fun setWelcomeSeen(seen: Boolean)

    fun currentStatus(): Boolean

    fun currentWelcomeSeen(): Boolean
}
