package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.repositories.IRemoteConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteConfigUseCasesTest {
    @Test
    fun observePrivacyPolicyUrlEmitsRepositoryValue() =
        runTest {
            val repository = FakeRemoteConfigRepository("https://example.com/privacy")
            val useCase = ObservePrivacyPolicyUrlUseCase(repository)

            val value = useCase().first()

            assertEquals("https://example.com/privacy", value)
        }
}

private class FakeRemoteConfigRepository(initialUrl: String) : IRemoteConfigRepository {
    private val privacyPolicyUrl = MutableStateFlow(initialUrl)

    override fun observePrivacyPolicyUrl(): Flow<String> = privacyPolicyUrl

    override suspend fun refresh() {
    }
}
