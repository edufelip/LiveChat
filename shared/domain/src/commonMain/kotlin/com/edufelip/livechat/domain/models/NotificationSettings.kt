package com.edufelip.livechat.domain.models

enum class NotificationSound(
    val id: String,
    private val legacyLabels: Set<String>,
) {
    Popcorn(id = "popcorn", legacyLabels = setOf("Popcorn")),
    Chime(id = "chime", legacyLabels = setOf("Chime")),
    Ripple(id = "ripple", legacyLabels = setOf("Ripple")),
    Silent(id = "silent", legacyLabels = setOf("Silent")),
    ;

    companion object {
        val default = Popcorn

        fun fromId(rawValue: String?): NotificationSound? {
            val normalized = rawValue?.trim()?.lowercase().orEmpty()
            if (normalized.isBlank()) return null
            return entries.firstOrNull { it.id == normalized }
        }

        fun fromLegacyLabel(rawValue: String?): NotificationSound? {
            val normalized = rawValue?.trim().orEmpty()
            if (normalized.isBlank()) return null
            return entries.firstOrNull { sound ->
                sound.legacyLabels.any { it.equals(normalized, ignoreCase = true) }
            }
        }

        fun normalizeId(rawValue: String?): String {
            if (rawValue.isNullOrBlank()) return default.id
            return fromId(rawValue)?.id
                ?: fromLegacyLabel(rawValue)?.id
                ?: default.id
        }
    }
}

data class QuietHours(
    val from: String = "22:00",
    val to: String = "07:00",
)

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val sound: String = NotificationSound.Popcorn.id,
    val quietHoursEnabled: Boolean = false,
    val quietHours: QuietHours = QuietHours(),
    val inAppVibration: Boolean = true,
    val showMessagePreview: Boolean = true,
)
