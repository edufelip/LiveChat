package com.edufelip.livechat.ui.common.audio

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic sound player for notification tone previews.
 */
interface SoundPlayer {
    /**
     * Plays a notification sound by its name/id.
     *
     * @param soundName The name of the sound to play (e.g., "Popcorn", "Chime")
     */
    fun playNotificationSound(soundName: String)

    /**
     * Stops any currently playing sound.
     */
    fun stop()
}

/**
 * Remembers a platform-specific sound player instance.
 */
@Composable
expect fun rememberSoundPlayer(): SoundPlayer
