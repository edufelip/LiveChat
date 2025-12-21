package com.edufelip.livechat.data.bridge

import com.edufelip.livechat.data.remote.PhoneExistsBatchResult
import com.edufelip.livechat.data.remote.PhoneExistsSingleResult

interface ContactsRemoteBridge {
    suspend fun phoneExists(phoneE164: String): PhoneExistsSingleResult

    suspend fun phoneExistsMany(phones: List<String>): PhoneExistsBatchResult

    suspend fun isUserRegistered(phoneE164: String): Boolean
}
