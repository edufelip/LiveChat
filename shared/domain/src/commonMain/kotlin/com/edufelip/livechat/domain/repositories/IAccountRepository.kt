package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.AccountProfile
import kotlinx.coroutines.flow.Flow

interface IAccountRepository {
    fun observeAccountProfile(): Flow<AccountProfile?>

    suspend fun updateDisplayName(displayName: String)

    suspend fun updateStatusMessage(statusMessage: String)

    suspend fun updateEmail(email: String)

    suspend fun deleteAccount()
}
