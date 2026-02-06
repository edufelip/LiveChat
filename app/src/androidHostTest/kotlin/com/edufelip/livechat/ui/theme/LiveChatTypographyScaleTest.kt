package com.edufelip.livechat.ui.theme

import com.edufelip.livechat.domain.models.AppearanceSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveChatTypographyScaleTest {
    @Test
    fun scaledClampsBelowMinimum() {
        val belowMin = LiveChatTypography.scaled(0.1f)
        val expected = LiveChatTypography.scaled(AppearanceSettings.MIN_TEXT_SCALE)
        assertEquals(expected.bodyMedium.fontSize, belowMin.bodyMedium.fontSize)
    }

    @Test
    fun scaledClampsAboveMaximum() {
        val aboveMax = LiveChatTypography.scaled(2f)
        val expected = LiveChatTypography.scaled(AppearanceSettings.MAX_TEXT_SCALE)
        assertEquals(expected.bodyMedium.fontSize, aboveMax.bodyMedium.fontSize)
    }

    @Test
    fun scaledIncreasesBodyTextForLargerScale() {
        val smaller = LiveChatTypography.scaled(AppearanceSettings.MIN_TEXT_SCALE)
        val larger = LiveChatTypography.scaled(AppearanceSettings.MAX_TEXT_SCALE)
        assertTrue(larger.bodyMedium.fontSize.value > smaller.bodyMedium.fontSize.value)
    }
}
