package com.edufelip.livechat.notifications

import android.content.Context
import android.net.Uri
import com.edufelip.livechat.R
import com.edufelip.livechat.domain.models.NotificationSound

object NotificationSoundResolver {
    fun resolve(
        context: Context,
        soundId: String,
    ): Uri? {
        val normalized = NotificationSound.normalizeId(soundId)
        val resId =
            when (normalized) {
                NotificationSound.Popcorn.id -> R.raw.notification_popcorn
                NotificationSound.Chime.id -> R.raw.notification_chime
                NotificationSound.Ripple.id -> R.raw.notification_ripple
                NotificationSound.Silent.id -> null
                else -> R.raw.notification_popcorn
            }
        return resId?.let { Uri.parse("android.resource://${context.packageName}/$it") }
    }
}
