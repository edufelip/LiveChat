package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IRemoteConfigRemoteData
import com.edufelip.livechat.domain.config.RemoteConfigDefaults
import com.edufelip.livechat.domain.config.RemoteConfigKeys
import com.edufelip.livechat.domain.repositories.IRemoteConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteConfigRepository(
    private val remoteData: IRemoteConfigRemoteData,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IRemoteConfigRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val privacyPolicyUrlState = MutableStateFlow(RemoteConfigDefaults.PRIVACY_POLICY_URL)

    init {
        scope.launch {
            updatePrivacyPolicyUrl()
        }
    }

    override fun observePrivacyPolicyUrl(): Flow<String> = privacyPolicyUrlState.asStateFlow()

    override suspend fun refresh() {
        runCatching { remoteData.fetchAndActivate() }
        updatePrivacyPolicyUrl()
    }

    private fun updatePrivacyPolicyUrl() {
        val candidate = remoteData.getString(RemoteConfigKeys.PRIVACY_POLICY_URL).trim()
        privacyPolicyUrlState.value =
            if (candidate.isNotBlank()) {
                candidate
            } else {
                RemoteConfigDefaults.PRIVACY_POLICY_URL
            }
    }
}
