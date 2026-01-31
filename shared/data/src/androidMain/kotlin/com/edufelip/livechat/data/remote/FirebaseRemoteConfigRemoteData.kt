package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.contracts.IRemoteConfigRemoteData
import com.edufelip.livechat.domain.config.RemoteConfigDefaults
import com.edufelip.livechat.domain.config.RemoteConfigKeys
import com.edufelip.livechat.shared.data.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

class FirebaseRemoteConfigRemoteData(
    private val remoteConfig: FirebaseRemoteConfig,
) : IRemoteConfigRemoteData {
    init {
        if (BuildConfig.DEBUG) {
            val settings =
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0)
                    .build()
            remoteConfig.setConfigSettingsAsync(settings)
        }
        remoteConfig.setDefaultsAsync(
            mapOf(
                RemoteConfigKeys.PRIVACY_POLICY_URL to RemoteConfigDefaults.PRIVACY_POLICY_URL,
            ),
        )
    }

    override suspend fun fetchAndActivate(): Boolean = remoteConfig.fetchAndActivate().await()

    override fun getString(key: String): String = remoteConfig.getString(key)
}
