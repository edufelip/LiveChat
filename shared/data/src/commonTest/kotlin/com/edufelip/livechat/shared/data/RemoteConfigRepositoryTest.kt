package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.contracts.IRemoteConfigRemoteData
import com.edufelip.livechat.data.repositories.RemoteConfigRepository
import com.edufelip.livechat.domain.config.RemoteConfigDefaults
import com.edufelip.livechat.domain.config.RemoteConfigKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteConfigRepositoryTest {
    @Test
    fun refreshUsesDefaultWhenRemoteValueIsBlank() =
        runTest {
            val remoteData = FakeRemoteConfigRemoteData(mutableMapOf(RemoteConfigKeys.PRIVACY_POLICY_URL to ""))
            val repository = RemoteConfigRepository(remoteData, StandardTestDispatcher(testScheduler))

            repository.refresh()
            val value = repository.observePrivacyPolicyUrl().first()

            assertEquals(RemoteConfigDefaults.PRIVACY_POLICY_URL, value)
        }

    @Test
    fun refreshUpdatesValueWhenRemoteConfigAvailable() =
        runTest {
            val remoteData = FakeRemoteConfigRemoteData(mutableMapOf(RemoteConfigKeys.PRIVACY_POLICY_URL to ""))
            val repository = RemoteConfigRepository(remoteData, StandardTestDispatcher(testScheduler))

            remoteData.values[RemoteConfigKeys.PRIVACY_POLICY_URL] = "https://example.com/privacy"
            repository.refresh()
            val value = repository.observePrivacyPolicyUrl().first()

            assertEquals("https://example.com/privacy", value)
        }
}

private class FakeRemoteConfigRemoteData(
    val values: MutableMap<String, String>,
) : IRemoteConfigRemoteData {
    override suspend fun fetchAndActivate(): Boolean = true

    override fun getString(key: String): String = values[key].orEmpty()
}
