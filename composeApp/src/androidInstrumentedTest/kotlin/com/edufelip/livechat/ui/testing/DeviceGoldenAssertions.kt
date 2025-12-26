package com.edufelip.livechat.ui.testing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File

internal object DeviceGoldenAssertions {
    private const val GOLDEN_DIR = "goldens"
    private const val RECORD_ARG = "recordGoldens"

    fun assertDeviceGolden(name: String) {
        if (shouldRecordGoldens()) {
            saveDeviceScreenshot(name)
            return
        }

        val actualFile = saveDeviceScreenshot("${name}_actual")
        val actual = BitmapFactory.decodeFile(actualFile.absolutePath)
        val expected = loadGolden(name)
        if (expected == null) {
            throw AssertionError("Golden '$name' not found in assets/$GOLDEN_DIR. Captured image stored in external files.")
        }
        if (actual == null) {
            throw AssertionError("Golden '$name' mismatch. Captured image stored in external files.")
        }
        val croppedActual = cropStatusBar(actual)
        val croppedExpected = cropStatusBar(expected)
        if (!croppedActual.sameAs(croppedExpected)) {
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

    private fun saveDeviceScreenshot(name: String): File {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(targetContext.filesDir, GOLDEN_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$name.png")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.waitForIdle()
        if (!device.takeScreenshot(file)) {
            throw AssertionError("Failed to capture device screenshot for '$name'.")
        }
        return file
    }

    private fun cropStatusBar(bitmap: Bitmap): Bitmap {
        val statusBarHeight = statusBarHeightPx()
        if (statusBarHeight <= 0 || statusBarHeight >= bitmap.height) {
            return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, statusBarHeight, bitmap.width, bitmap.height - statusBarHeight)
    }

    private fun statusBarHeightPx(): Int {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else {
            0
        }
    }
}
