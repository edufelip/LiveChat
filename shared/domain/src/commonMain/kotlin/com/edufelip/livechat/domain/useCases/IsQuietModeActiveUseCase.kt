package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.NotificationSettings
import kotlinx.datetime.LocalTime

class IsQuietModeActiveUseCase {
    /**
     * Checks if the current time falls within the configured quiet hours window.
     * @param settings The notification settings containing quiet hours configuration.
     * @param currentTime The current local time.
     * @return True if quiet mode is active, False otherwise.
     */
    operator fun invoke(
        settings: NotificationSettings,
        currentTime: LocalTime,
    ): Boolean {
        if (!settings.quietHoursEnabled) return false

        val fromStr = settings.quietHours.from
        val toStr = settings.quietHours.to

        val from = parseTime(fromStr) ?: return false
        val to = parseTime(toStr) ?: return false

        return if (from < to) {
            // Case: 09:00 to 17:00
            currentTime in from..to
        } else {
            // Case: 22:00 to 07:00 (spans midnight)
            currentTime >= from || currentTime <= to
        }
    }

    private fun parseTime(timeStr: String): LocalTime? {
        return try {
            val parts = timeStr.split(":")
            if (parts.size != 2) return null
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            LocalTime(hour, minute)
        } catch (e: Exception) {
            null
        }
    }
}
