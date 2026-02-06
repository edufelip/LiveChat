package com.edufelip.livechat.ui.features.settings.appearance

import com.edufelip.livechat.domain.models.AppearanceSettings
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppearanceTextScaleTest {
    @Test
    fun scaleFromSliderUsesExpectedBounds() {
        assertEquals(AppearanceSettings.MIN_TEXT_SCALE, scaleFromSlider(0f))
        assertEquals(AppearanceSettings.MAX_TEXT_SCALE, scaleFromSlider(100f))
    }

    @Test
    fun sliderFromScaleUsesExpectedBounds() {
        assertEquals(0f, sliderFromScale(AppearanceSettings.MIN_TEXT_SCALE))
        assertEquals(100f, sliderFromScale(AppearanceSettings.MAX_TEXT_SCALE))
    }

    @Test
    fun scaleAndSliderRoundTripWithinTolerance() {
        val values =
            listOf(
                AppearanceSettings.MIN_TEXT_SCALE,
                AppearanceSettings.DEFAULT_TEXT_SCALE,
                1.1f,
                AppearanceSettings.MAX_TEXT_SCALE,
            )

        values.forEach { scale ->
            val roundTrip = scaleFromSlider(sliderFromScale(scale))
            assertTrue(abs(roundTrip - scale) <= 0.0001f)
        }
    }
}
