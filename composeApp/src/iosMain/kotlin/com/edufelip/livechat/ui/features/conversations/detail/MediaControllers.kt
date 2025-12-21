@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.livechat.ui.features.conversations.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerController.Companion.isSourceTypeAvailable
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

@Composable
actual fun rememberConversationMediaController(): ConversationMediaController = remember { IosConversationMediaController() }

private class IosConversationMediaController : ConversationMediaController {
    private var recorder: AVAudioRecorder? = null
    private var recorderPath: String? = null
    private var autoStopJob: Job? = null

    override suspend fun pickImage(): MediaResult<String> {
        return when (ensurePhotoPermission()) {
            PermissionStatus.GRANTED -> {
                val path =
                    presentImagePicker(
                        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
                    )
                if (path != null) MediaResult.Success(path) else MediaResult.Cancelled
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }
    }

    override suspend fun capturePhoto(): MediaResult<String> {
        return when (ensureCameraPermission()) {
            PermissionStatus.GRANTED -> {
                val path =
                    presentImagePicker(
                        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                    )
                if (path != null) MediaResult.Success(path) else MediaResult.Cancelled
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }
    }

    override suspend fun startAudioRecording(): MediaResult<Unit> {
        if (recorder != null) return MediaResult.Success(Unit)
        return when (ensureMicPermission()) {
            PermissionStatus.GRANTED -> {
                val started =
                    withContext(Dispatchers.Default) {
                        val session = AVAudioSession.sharedInstance()
                        session.setCategory(
                            category = AVAudioSessionCategoryPlayAndRecord,
                            mode = AVAudioSessionModeDefault,
                            options = 0u,
                            error = null,
                        )
                        session.setActive(active = true, withOptions = 0u, error = null)
                        val tempPath = (NSTemporaryDirectory() ?: "/tmp/") + "aud_${NSUUID().UUIDString}.m4a"
                        val settings =
                            mapOf<Any?, Any?>(
                                AVFormatIDKey to kAudioFormatMPEG4AAC,
                                AVSampleRateKey to 44100.0,
                                AVNumberOfChannelsKey to 1,
                                AVEncoderAudioQualityKey to 96,
                            )
                        val recorderInstance =
                            AVAudioRecorder(
                                uRL = NSURL.fileURLWithPath(tempPath),
                                settings = settings,
                                error = null,
                            )
                        if (recorderInstance == null || !recorderInstance.prepareToRecord()) return@withContext false
                        recorderInstance.record()
                        recorder = recorderInstance
                        recorderPath = tempPath
                        autoStopJob =
                            CoroutineScope(Dispatchers.Default).launch {
                                delay(60_000)
                                stopAudioRecording()
                            }
                        true
                    }
                if (started) MediaResult.Success(Unit) else MediaResult.Error("Unable to start recording")
            }
            PermissionStatus.DENIED -> MediaResult.Permission(PermissionStatus.DENIED)
            PermissionStatus.BLOCKED -> MediaResult.Permission(PermissionStatus.BLOCKED)
        }
    }

    override suspend fun stopAudioRecording(): String? =
        withContext(Dispatchers.Default) {
            val current = recorder ?: return@withContext null
            val path = recorderPath
            try {
                current.stop()
            } catch (_: Throwable) {
            }
            recorder = null
            recorderPath = null
            autoStopJob?.cancel()
            path
        }

    private suspend fun presentImagePicker(sourceType: UIImagePickerControllerSourceType): String? =
        suspendCancellableCoroutine { cont ->
            dispatch_async(dispatch_get_main_queue()) {
                if (!isSourceTypeAvailable(sourceType)) {
                    cont.resume(null)
                    return@dispatch_async
                }
                val controller = UIImagePickerController()
                controller.sourceType = sourceType
                val delegate =
                    PickerDelegate { path, _ ->
                        controller.dismissViewControllerAnimated(true, completion = null)
                        cont.resume(path)
                    }
                controller.delegate = delegate
                val root = currentRootController()
                if (root == null) {
                    cont.resume(null)
                    return@dispatch_async
                }
                root.presentViewController(controller, animated = true, completion = null)
            }
            cont.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    currentRootController()?.dismissViewControllerAnimated(true, completion = null)
                }
            }
        }

    private fun currentRootController(): UIViewController? {
        val app = UIApplication.sharedApplication
        val window =
            app.windows
                ?.mapNotNull { it as? UIWindow }
                ?.firstOrNull { it.isKeyWindow() }
        return window?.rootViewController
    }

    private class PickerDelegate(
        private val onResult: (String?, Throwable?) -> Unit,
    ) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            onResult(null, null)
        }

        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>,
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            if (image == null) {
                onResult(null, null)
                return
            }
            val data = UIImageJPEGRepresentation(image, 0.9)
            if (data == null) {
                onResult(null, null)
                return
            }
            val path = (NSTemporaryDirectory() ?: "/tmp/") + "img_${NSUUID().UUIDString}.jpg"
            val ok = data.writeToFile(path, atomically = true)
            if (ok) {
                onResult(path, null)
            } else {
                onResult(null, null)
            }
        }
    }
}

private suspend fun ensureMicPermission(): PermissionStatus {
    val session = AVAudioSession.sharedInstance()
    return when (session.recordPermission) {
        AVAudioSessionRecordPermissionGranted -> PermissionStatus.GRANTED
        AVAudioSessionRecordPermissionUndetermined -> {
            val granted =
                suspendCancellableCoroutine<Boolean> { cont ->
                    session.requestRecordPermission { ok: Boolean ->
                        cont.resume(ok)
                    }
                }
            if (granted) PermissionStatus.GRANTED else PermissionStatus.BLOCKED
        }
        AVAudioSessionRecordPermissionDenied -> PermissionStatus.BLOCKED
        else -> PermissionStatus.BLOCKED
    }
}

private suspend fun ensureCameraPermission(): PermissionStatus {
    val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    return when (status) {
        AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
        AVAuthorizationStatusDenied,
        AVAuthorizationStatusRestricted,
        -> PermissionStatus.BLOCKED
        AVAuthorizationStatusNotDetermined -> {
            val granted =
                suspendCancellableCoroutine<Boolean> { cont ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { ok: Boolean ->
                        cont.resume(ok)
                    }
                }
            if (granted) PermissionStatus.GRANTED else PermissionStatus.BLOCKED
        }
        else -> PermissionStatus.BLOCKED
    }
}

private suspend fun ensurePhotoPermission(): PermissionStatus {
    val status = PHPhotoLibrary.authorizationStatus()
    return when (status) {
        PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> PermissionStatus.GRANTED
        PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> PermissionStatus.BLOCKED
        PHAuthorizationStatusNotDetermined -> {
            val granted =
                suspendCancellableCoroutine<Boolean> { cont ->
                    PHPhotoLibrary.requestAuthorization { newStatus: PHAuthorizationStatus ->
                        cont.resume(
                            newStatus == PHAuthorizationStatusAuthorized ||
                                newStatus == PHAuthorizationStatusLimited,
                        )
                    }
                }
            if (granted) PermissionStatus.GRANTED else PermissionStatus.BLOCKED
        }
        else -> PermissionStatus.BLOCKED
    }
}
