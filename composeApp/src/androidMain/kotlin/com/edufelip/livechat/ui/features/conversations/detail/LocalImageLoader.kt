package com.edufelip.livechat.ui.features.conversations.detail

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun loadLocalImageBitmap(path: String): ImageBitmap? {
    val bitmap = BitmapFactory.decodeFile(path) ?: return null
    return bitmap.asImageBitmap()
}

