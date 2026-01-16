package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.NotificationSettings
import com.edufelip.livechat.domain.models.QuietHours
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsQuietModeActiveUseCaseTest {
    private val useCase = IsQuietModeActiveUseCase()

    @Test
    fun should_return_false_when_quiet_hours_are_disabled() {
        val settings = NotificationSettings(quietHoursEnabled = false)
        val result = useCase(settings, LocalTime(12, 0))
        assertFalse(result)
    }

    @Test
    fun should_return_true_when_time_is_within_range_same_day() {
        val settings =
            NotificationSettings(
                quietHoursEnabled = true,
                quietHours = QuietHours(from = "09:00", to = "17:00"),
            )
        // 12:00 is between 09:00 and 17:00
        assertTrue(useCase(settings, LocalTime(12, 0)))
    }

    @Test
    fun should_return_false_when_time_is_outside_range_same_day() {
        val settings =
            NotificationSettings(
                quietHoursEnabled = true,
                quietHours = QuietHours(from = "09:00", to = "17:00"),
            )
        // 08:00 is before 09:00
        assertFalse(useCase(settings, LocalTime(8, 0)))
        // 18:00 is after 17:00
        assertFalse(useCase(settings, LocalTime(18, 0)))
    }

    @Test
    fun should_return_true_when_time_is_within_range_spans_midnight() {
        val settings =
            NotificationSettings(
                quietHoursEnabled = true,
                quietHours = QuietHours(from = "22:00", to = "07:00"),
            )
        // 23:00 is after 22:00
        assertTrue(useCase(settings, LocalTime(23, 0)))
        // 01:00 is before 07:00
        assertTrue(useCase(settings, LocalTime(1, 0)))
        // 06:59 is before 07:00
        assertTrue(useCase(settings, LocalTime(6, 59)))
    }

    @Test
    fun should_return_false_when_time_is_outside_range_spans_midnight() {
        val settings =
            NotificationSettings(
                quietHoursEnabled = true,
                quietHours = QuietHours(from = "22:00", to = "07:00"),
            )
        // 21:00 is before 22:00
        assertFalse(useCase(settings, LocalTime(21, 0)))
        // 08:00 is after 07:00
        assertFalse(useCase(settings, LocalTime(8, 0)))
    }

    @Test
    fun should_handle_invalid_time_strings_gracefully() {
        val settings =
            NotificationSettings(
                quietHoursEnabled = true,
                quietHours = QuietHours(from = "invalid", to = "07:00"),
            )
        assertFalse(useCase(settings, LocalTime(12, 0)))
    }
}
