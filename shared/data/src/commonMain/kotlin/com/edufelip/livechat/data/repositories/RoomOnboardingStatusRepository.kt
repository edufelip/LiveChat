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
    private val statusFlow = dao.observe().map { it ?: OnboardingStatusEntity() }

    override val onboardingComplete: Flow<Boolean> =
        statusFlow.map { it.complete }

    override val welcomeSeen: Flow<Boolean> =
        statusFlow.map { it.welcomeSeen }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        val current = dao.current()
        dao.upsert(
            OnboardingStatusEntity(
                complete = complete,
                welcomeSeen = current?.welcomeSeen ?: false,
            ),
        )
    }

    override suspend fun setWelcomeSeen(seen: Boolean) {
        val current = dao.current()
        dao.upsert(
            OnboardingStatusEntity(
                complete = current?.complete ?: false,
                welcomeSeen = seen,
            ),
        )
    }

    override fun currentStatus(): Boolean = runBlocking { dao.current()?.complete ?: false }

    override fun currentWelcomeSeen(): Boolean = runBlocking { dao.current()?.welcomeSeen ?: false }
}
