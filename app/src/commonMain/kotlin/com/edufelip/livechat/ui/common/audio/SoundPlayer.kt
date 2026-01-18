package com.edufelip.livechat.ui.common.audio

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic sound player for notification tone previews.
 */
interface SoundPlayer {
    /**
     * Plays a notification sound by its name/id.
     *
     * @param soundName The stable sound ID to play (e.g., "popcorn", "chime")
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
