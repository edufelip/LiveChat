package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.ThemeMode
import com.edufelip.livechat.domain.repositories.IAppearanceSettingsRepository
import com.edufelip.livechat.domain.useCases.ObserveAppearanceSettingsUseCase
import com.edufelip.livechat.domain.useCases.UpdateTextScaleUseCase
import com.edufelip.livechat.domain.useCases.UpdateThemeModeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppearanceSettingsPresenterTest {
    @Test
    fun updateTextScaleClampsBeforeDispatchingToUseCase() =
        runBlocking {
            val repository = FakeAppearanceSettingsRepository()
            val presenter =
                AppearanceSettingsPresenter(
                    observeSettings = ObserveAppearanceSettingsUseCase(repository),
                    updateThemeModeUseCase = UpdateThemeModeUseCase(repository),
                    updateTextScaleUseCase = UpdateTextScaleUseCase(repository),
                    scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
                )

            waitForPropagation()
            presenter.updateTextScale(2f)
            assertTrue(repository.awaitUpdate())
            waitForCondition { !presenter.state.value.isUpdating }

            assertEquals(
                expected = AppearanceSettings.MAX_TEXT_SCALE,
                actual = repository.lastUpdatedTextScale ?: error("Missing update result"),
                absoluteTolerance = 0.0001f,
            )
            assertEquals(
                expected = AppearanceSettings.MAX_TEXT_SCALE,
                actual = presenter.state.value.settings.textScale,
                absoluteTolerance = 0.0001f,
            )

            presenter.close()
        }

    private suspend fun waitForPropagation() {
        repeat(20) {
            delay(10)
        }
    }

    private suspend fun waitForCondition(condition: () -> Boolean) {
        repeat(100) {
            if (condition()) return
            delay(10)
        }
        assertTrue(condition())
    }
}

private class FakeAppearanceSettingsRepository : IAppearanceSettingsRepository {
    private val state = MutableStateFlow(AppearanceSettings())
    var lastUpdatedTextScale: Float? = null
        private set
    private val updateLatch = CountDownLatch(1)

    override fun observeSettings(): Flow<AppearanceSettings> = state.asStateFlow()

    override suspend fun updateThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun updateTextScale(scale: Float) {
        lastUpdatedTextScale = scale
        state.value = state.value.copy(textScale = scale)
        updateLatch.countDown()
    }

    override suspend fun resetSettings() {
        state.value = AppearanceSettings()
    }

    fun awaitUpdate(timeoutMs: Long = 1000): Boolean = updateLatch.await(timeoutMs, TimeUnit.MILLISECONDS)
}
