package com.edufelip.livechat.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PhoneNumberFormattingServiceTest {
    private val formatter = phoneNumberFormattingService()

    @Test
    fun formatAsYouTypePreservesDigits() {
        val raw = "6505553434"
        val formatted = formatter.formatAsYouType(raw, "US")
        assertEquals(raw, formatter.normalizeDigits(formatted))
    }

    @Test
    fun formatAsYouTypeEmptyIsEmpty() {
        assertEquals("", formatter.formatAsYouType("", "US"))
    }

    @Test
    fun formatAsYouTypeBrazilWrapsAreaCodeWithParentheses() {
        val formatted = formatter.formatAsYouType("21985670564", "BR")
        assertEquals("(21) 98567-0564", formatted)
    }

    @Test
    fun exampleNumberHasDigits() {
        val example = formatter.exampleNumber("US")
        assertNotNull(example)
        assertTrue(formatter.normalizeDigits(example).length >= 7)
    }
}
