package com.edufelip.livechat.ui.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzyMatcherTest {
    @Test
    fun `exact match should return true`() {
        assertTrue(FuzzyMatcher.matches("notification", "notification"))
        assertTrue(FuzzyMatcher.matches("dark", "dark"))
    }

    @Test
    fun `case insensitive exact match should return true`() {
        assertTrue(FuzzyMatcher.matches("Notification", "notification"))
        assertTrue(FuzzyMatcher.matches("DARK", "dark"))
        assertTrue(FuzzyMatcher.matches("DaRk", "dark"))
    }

    @Test
    fun `single character typo should match`() {
        assertTrue(FuzzyMatcher.matches("notifcation", "notification")) // missing 'i'
        assertTrue(FuzzyMatcher.matches("notificationn", "notification")) // extra 'n'
        assertTrue(FuzzyMatcher.matches("votification", "notification")) // 'n' -> 'v'
    }

    @Test
    fun `substring match should return true`() {
        assertTrue(FuzzyMatcher.matches("notif", "notification"))
        assertTrue(FuzzyMatcher.matches("tion", "notification"))
        assertTrue(FuzzyMatcher.matches("fica", "notification"))
    }

    @Test
    fun `two character typos should match`() {
        assertTrue(FuzzyMatcher.matches("notifcaton", "notification")) // missing 'i' and transposed 'io'
        assertTrue(FuzzyMatcher.matches("votificaton", "notification")) // 'n'->'v' and transposed 'io'
    }

    @Test
    fun `completely different strings should not match`() {
        assertFalse(FuzzyMatcher.matches("account", "notification"))
        assertFalse(FuzzyMatcher.matches("dark", "light"))
        assertFalse(FuzzyMatcher.matches("abc", "xyz"))
    }

    @Test
    fun `three or more typos should not match`() {
        assertFalse(FuzzyMatcher.matches("vtifcaton", "notification")) // too many errors
        assertFalse(FuzzyMatcher.matches("abcdefg", "notification"))
    }

    @Test
    fun `empty query should not match anything`() {
        assertFalse(FuzzyMatcher.matches("", "notification"))
        assertFalse(FuzzyMatcher.matches("   ", "notification"))
    }

    @Test
    fun `empty target should not match non-empty query`() {
        assertFalse(FuzzyMatcher.matches("notification", ""))
    }

    @Test
    fun `both empty should match`() {
        assertTrue(FuzzyMatcher.matches("", ""))
    }

    @Test
    fun `query longer than target plus threshold should not match`() {
        assertFalse(FuzzyMatcher.matches("notificationssss", "notification"))
    }

    @Test
    fun `transposed characters should match`() {
        assertTrue(FuzzyMatcher.matches("notificaiton", "notification")) // 'ti' -> 'it'
        assertTrue(FuzzyMatcher.matches("darks", "dark")) // extra char at end
    }

    @Test
    fun `real world typos should match`() {
        // Common typos users make
        assertTrue(FuzzyMatcher.matches("notifcation", "notification"))
        assertTrue(FuzzyMatcher.matches("appearence", "appearance"))
        assertTrue(FuzzyMatcher.matches("accout", "account"))
        assertTrue(FuzzyMatcher.matches("privicy", "privacy"))
        assertTrue(FuzzyMatcher.matches("contacts", "contacts"))
    }

    @Test
    fun `match returns false when distance exceeds threshold`() {
        // This should exceed default threshold of 2
        assertFalse(FuzzyMatcher.matches("xyz", "notification"))
        assertFalse(FuzzyMatcher.matches("abc", "notification"))
    }
}
