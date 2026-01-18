package com.edufelip.livechat.ui.common.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.livechat.domain.models.NotificationSound
import platform.AudioToolbox.AudioServicesPlaySystemSound

class IosSoundPlayer : SoundPlayer {
    override fun playNotificationSound(soundName: String) {
        if (soundName == NotificationSound.Silent.id) return

        // On iOS, we can play a standard system alert sound (1007 is typically the 'Note' sound)
        // In a real app, we would load the actual asset file.
        AudioServicesPlaySystemSound(1007u)
    }

    override fun stop() {
        // System sounds are short and fire-and-forget
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    return remember { IosSoundPlayer() }
}
