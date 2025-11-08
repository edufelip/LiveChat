package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.OnboardingStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class RoomOnboardingStatusRepository(
    private val database: LiveChatDatabase,
) : IOnboardingStatusRepository {

    private val dao = database.onboardingStatusDao()

    override val onboardingComplete: Flow<Boolean> =
        dao.observe().map { it ?: false }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        dao.upsert(OnboardingStatusEntity(complete = complete))
    }

    override fun currentStatus(): Boolean =
        runBlocking { dao.current() ?: false }
}
