package com.edufelip.livechat.domain.repositories

import kotlinx.coroutines.flow.Flow

interface IRemoteConfigRepository {
    fun observePrivacyPolicyUrl(): Flow<String>

    suspend fun refresh()
}
