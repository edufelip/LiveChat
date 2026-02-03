package com.edufelip.livechat.ui.testing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream

internal object GoldenAssertions {
    private const val GOLDEN_DIR = "goldens"
    private const val RECORD_ARG = "recordGoldens"

    fun assertGolden(
        rule: ComposeTestRule,
        name: String,
    ) {
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        if (shouldRecordGoldens()) {
            saveBitmap(bitmap, name)
            return
        }

        val expected = loadGolden(name)
        if (expected == null) {
            saveBitmap(bitmap, "${name}_missing")
            throw AssertionError("Golden '$name' not found in assets/$GOLDEN_DIR. Captured image stored in external files.")
        }
        if (!bitmap.sameAs(expected)) {
            saveBitmap(bitmap, "${name}_actual")
            throw AssertionError("Golden '$name' mismatch. Captured image stored in external files.")
        }
    }

    private fun shouldRecordGoldens(): Boolean {
        val args = InstrumentationRegistry.getArguments()
        return args.getString(RECORD_ARG)?.equals("true", ignoreCase = true) == true
    }

    private fun loadGolden(name: String): Bitmap? {
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetPath = "$GOLDEN_DIR/$name.png"
        return runCatching {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    private fun saveBitmap(
        bitmap: Bitmap,
        name: String,
    ) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(targetContext.filesDir, GOLDEN_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$name.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
