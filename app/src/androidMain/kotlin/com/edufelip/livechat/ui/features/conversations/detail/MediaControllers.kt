package com.edufelip.livechat.ui.features.conversations.detail

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberConversationMediaController(): ConversationMediaController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pendingPermissionState = remember { mutableStateOf<String?>(null) }
    val permissionContinuationState =
        remember { mutableStateOf<CompletableDeferred<PermissionStatus>?>(null) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val permission = pendingPermissionState.value
            val activity = context.findActivity()
            val blocked =
                !granted &&
                    permission != null &&
                    activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) &&
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            val status =
                when {
                    granted -> PermissionStatus.GRANTED
                    blocked -> PermissionStatus.BLOCKED
                    else -> PermissionStatus.DENIED
                }
            permissionContinuationState.value?.complete(status)
            permissionContinuationState.value = null
            pendingPermissionState.value = null
        }

    val imageContinuationState = remember { mutableStateOf<CompletableDeferred<String?>?>(null) }
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            scope.launch {
                val path = uri?.let { copyUriToCache(context, it, "img_", "jpg") }
                imageContinuationState.value?.complete(path)
                imageContinuationState.value = null
            }
        }

    val cameraContinuationState = remember { mutableStateOf<CompletableDeferred<String?>?>(null) }
    val pendingCameraUriState = remember { mutableStateOf<Uri?>(null) }
    val takePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            scope.launch {
                val path =
                    if (success) pendingCameraUriState.value?.let { uri -> uri.toFilePath(context) } else null
                cameraContinuationState.value?.complete(path)
                cameraContinuationState.value = null
                pendingCameraUriState.value = null
            }
        }

    val controller =
        remember {
            AndroidConversationMediaController(
                context = context,
                requestPermission = { permission ->
                    val alreadyGranted =
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    if (alreadyGranted) return@AndroidConversationMediaController CompletableDeferred(PermissionStatus.GRANTED)
                    val deferred = CompletableDeferred<PermissionStatus>()
                    permissionContinuationState.value = deferred
                    pendingPermissionState.value = permission
                    permissionLauncher.launch(permission)
                    deferred
                },
                pickImageAction = {
                    val deferred = CompletableDeferred<String?>()
                    imageContinuationState.value = deferred
                    pickImageLauncher.launch("image/*")
                    deferred
                },
                capturePhotoAction = {
                    val deferred = CompletableDeferred<String?>()
                    cameraContinuationState.value = deferred
                    val outputFile = createTempImageFile(context)
                    pendingCameraUriState.value = outputFile.second
                    takePhotoLauncher.launch(outputFile.second)
                    deferred
                },
            )
        }

    return controller
}

private class AndroidConversationMediaController(
    private val context: Context,
    private val requestPermission: (String) -> CompletableDeferred<PermissionStatus>,
    private val pickImageAction: () -> CompletableDeferred<String?>,
    private val capturePhotoAction: () -> CompletableDeferred<String?>,
) : ConversationMediaController {
    private var recorder: MediaRecorder? = null
    private var recorderFile: File? = null
    private var autoStopJob: Job? = null

    override suspend fun pickImage(): MediaResult<String> =
        when (ensureReadImagesPermission()) {
            PermissionStatus.GRANTED -> {
                val path = pickImageAction().awaitResult()
                if (path != null) MediaResult.Success(path) else MediaResult.Cancelled
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }

    override suspend fun capturePhoto(): MediaResult<String> =
        when (ensureCameraPermission()) {
            PermissionStatus.GRANTED -> {
                val path = capturePhotoAction().awaitResult()
                if (path != null) MediaResult.Success(path) else MediaResult.Cancelled
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }

    override suspend fun startAudioRecording(): MediaResult<Unit> {
        if (recorder != null) return MediaResult.Success(Unit)
        return when (ensureAudioPermission()) {
            PermissionStatus.GRANTED -> {
                val started =
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val output = File.createTempFile("aud_", ".m4a", context.cacheDir)
                            val mediaRecorder = MediaRecorder()
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            mediaRecorder.setAudioEncodingBitRate(128_000)
                            mediaRecorder.setAudioSamplingRate(44_100)
                            mediaRecorder.setOutputFile(output.absolutePath)
                            mediaRecorder.prepare()
                            mediaRecorder.start()
                            recorder = mediaRecorder
                            recorderFile = output
                            autoStopJob =
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    delay(60_000)
                                    stopAudioRecording()
                                }
                            true
                        }.getOrDefault(false)
                    }
                if (started) MediaResult.Success(Unit) else MediaResult.Error()
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }
    }

    override suspend fun stopAudioRecording(): String? =
        withContext(Dispatchers.IO) {
            val current = recorder ?: return@withContext null
            val output = recorderFile
            try {
                current.stop()
            } catch (_: Throwable) {
                // ignore
            } finally {
                current.reset()
                current.release()
                recorder = null
                recorderFile = null
                autoStopJob?.cancel()
            }
            output?.absolutePath
        }

    private suspend fun ensureReadImagesPermission(): PermissionStatus {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        return ensurePermission(permission)
    }

    private suspend fun ensureCameraPermission(): PermissionStatus = ensurePermission(Manifest.permission.CAMERA)

    private suspend fun ensureAudioPermission(): PermissionStatus = ensurePermission(Manifest.permission.RECORD_AUDIO)

    private suspend fun ensurePermission(permission: String): PermissionStatus {
        val deferred = requestPermission(permission)
        val status = deferred.awaitResult()
        if (status == PermissionStatus.GRANTED) return status
        val activity = context.findActivity()
        val blocked =
            activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) &&
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        return if (blocked) PermissionStatus.BLOCKED else status
    }
}

private suspend fun <T> CompletableDeferred<T>.awaitResult(): T =
    suspendCancellableCoroutine { cont ->
        invokeOnCompletion { throwable ->
            if (throwable != null) {
                cont.resumeWithException(throwable)
            } else {
                runCatching { getCompleted() }
                    .onSuccess { cont.resume(it) }
                    .onFailure { error -> cont.resumeWithException(error) }
            }
        }
    }

private suspend fun copyUriToCache(
    context: Context,
    uri: Uri,
    prefix: String,
    extension: String,
): String? =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val input: InputStream = resolver.openInputStream(uri) ?: return@withContext null
        val cacheFile = File.createTempFile(prefix, ".$extension", context.cacheDir)
        cacheFile.outputStream().use { output ->
            input.use { inp -> inp.copyTo(output) }
        }
        cacheFile.absolutePath
    }

private fun createTempImageFile(context: Context): Pair<File, Uri> {
    val file = File.createTempFile("photo_", ".jpg", context.cacheDir)
    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    return file to uri
}

private fun Uri.toFilePath(context: Context): String? =
    try {
        val file = File(context.cacheDir, "captured_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(this)?.use { input ->
            BufferedOutputStream(file.outputStream()).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (_: Throwable) {
        null
    }

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
