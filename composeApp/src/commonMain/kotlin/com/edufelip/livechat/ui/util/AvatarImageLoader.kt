package com.edufelip.livechat.ui.util

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun loadAvatarImageBitmap(
    photoUrl: String,
    platformContext: Any?,
): ImageBitmap?
