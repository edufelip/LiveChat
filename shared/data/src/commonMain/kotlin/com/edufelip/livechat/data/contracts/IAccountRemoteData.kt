package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.AccountProfile

interface IAccountRemoteData {
    suspend fun fetchAccountProfile(
        userId: String,
        idToken: String,
    ): AccountProfile?

    suspend fun updateDisplayName(
        userId: String,
        idToken: String,
        displayName: String,
    )

    suspend fun updateStatusMessage(
        userId: String,
        idToken: String,
        statusMessage: String,
    )

    suspend fun updateEmail(
        userId: String,
        idToken: String,
        email: String,
    )

    suspend fun ensureUserDocument(
        userId: String,
        idToken: String,
        phoneNumber: String?,
    )

    suspend fun deleteAccount(
        userId: String,
        idToken: String,
    )
}
