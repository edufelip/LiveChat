package com.edufelip.livechat.ui.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

actual suspend fun loadAvatarImageBitmap(
    photoUrl: String,
    platformContext: Any?,
): ImageBitmap? {
    val context = platformContext as? Context ?: return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(photoUrl)
            val inputStream =
                when (uri.scheme?.lowercase()) {
                    "http", "https" -> URL(photoUrl).openStream()
                    else -> context.contentResolver.openInputStream(uri)
                }
            inputStream?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        }.getOrNull()
    }
}
