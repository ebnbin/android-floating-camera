package com.ebnbin.floatingcamera.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.dev.DevHelper
import com.ebnbin.floatingcamera.fragment.preference.CameraPreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.WindowPreferenceFragment
import com.ebnbin.floatingcamera.service.CameraService
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.CameraHelper
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.LocalBroadcastHelper
import com.ebnbin.floatingcamera.util.PermissionHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.cameraManager
import com.ebnbin.floatingcamera.util.extension.fileFormatExtension
import com.ebnbin.floatingcamera.util.sp
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Semaphore

/**
 * 相机控件.
 */
@SuppressLint("ViewConstructor")
open class CameraView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        TextureView(context, attrs, defStyleAttr, defStyleRes),
        TextureView.SurfaceTextureListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        RotationHelper.Listener {
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        invalidateTransform()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        closeCamera()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

    //*****************************************************************************************************************

    private var enableToast = WindowPreferenceFragment.enableToast

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CameraPreferenceFragment.KEY_IS_FRONT,
            CameraPreferenceFragment.KEY_BACK_IS_PHOTO,
            CameraPreferenceFragment.KEY_BACK_VIDEO_PROFILE,
            CameraPreferenceFragment.KEY_BACK_PHOTO_RESOLUTION,
            CameraPreferenceFragment.KEY_FRONT_IS_PHOTO,
            CameraPreferenceFragment.KEY_FRONT_VIDEO_PROFILE,
            CameraPreferenceFragment.KEY_FRONT_PHOTO_RESOLUTION -> {
                closeCamera()
                openCamera()
            }
            WindowPreferenceFragment.KEY_ENABLE_TOAST -> enableToast = WindowPreferenceFragment.enableToast
            WindowPreferenceFragment.KEY_IS_TOUCHABLE -> {
                if (!WindowPreferenceFragment.isTouchable && !isPhoto) stopRecord(true)
            }
        }
    }

    //*****************************************************************************************************************
    //*****************************************************************************************************************

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        surfaceTextureListener = this

        sp.registerOnSharedPreferenceChangeListener(this)

        RotationHelper.listeners.add(this)

        startBackgroundThread()
    }

    override fun onDetachedFromWindow() {
        stopBackgroundThread()

        RotationHelper.listeners.remove(this)

        sp.unregisterOnSharedPreferenceChangeListener(this)

        super.onDetachedFromWindow()
    }

    //*****************************************************************************************************************

    fun onTap() {
        if (isPhoto) {
            post { capture() }
        } else {
            post { toggleRecord() }
        }
    }

    //*****************************************************************************************************************

    fun finish() {
    }

    private fun error(message: String) {
        CameraService.stop()
        Crashlytics.logException(BaseRuntimeException("CameraView finish $message"))
    }

    //*****************************************************************************************************************

    override fun onRotationChanged(oldRotation: Int, newRotation: Int) {
        if (isAvailable) {
            invalidateTransform()
        }
    }

    //*****************************************************************************************************************

    private fun toast(text: CharSequence) {
        post {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    //*****************************************************************************************************************

    /**
     * 配置 [setTransform].
     */
    private fun invalidateTransform() {
        if (!isAttachedToWindow) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val viewCenterX = 0.5f * viewWidth
        val viewCenterY = 0.5f * viewHeight

        // 因为之后需要 rotate, 且以 0 度为标准, 因此这里使用 portraitWidth 和 portraitHeight.
        val bufferWidth = device.previewResolution.portraitWidth.toFloat()
        val bufferHeight = device.previewResolution.portraitHeight.toFloat()
        val bufferCenterX = 0.5f * bufferWidth
        val bufferCenterY = 0.5f * bufferHeight

        val offsetX = viewCenterX - bufferCenterX
        val offsetY = viewCenterY - bufferCenterY

        val viewRectF = RectF(0f, 0f, viewWidth, viewHeight)
        val bufferRectF = RectF(offsetX, offsetY, bufferWidth + offsetX, bufferHeight + offsetY)

        val rotation = display.rotation

        val matrix = Matrix()

        matrix.setRectToRect(viewRectF, bufferRectF, Matrix.ScaleToFit.FILL)

        val scale = Math.max(viewWidth / device.previewResolution.width(rotation),
                viewHeight / device.previewResolution.height(rotation))
        matrix.postScale(scale, scale, viewCenterX, viewCenterY)

        val degrees = 360f - rotation * 90f
        matrix.postRotate(degrees, viewCenterX, viewCenterY)

        setTransform(matrix)
    }

    //*****************************************************************************************************************

    /**
     * An additional thread for running tasks that shouldn't block the UI. This is used for all callbacks from the
     * [CameraDevice] and [CameraCaptureSession]s.
     */
    private lateinit var backgroundThread: HandlerThread

    /**
     * A [Handler] for running tasks in the background.
     */
    protected lateinit var backgroundHandler: Handler private set

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e("ebnbin", "", e)
        }
    }

    //*****************************************************************************************************************

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    protected var cameraOpenCloseLock = Semaphore(1)

    /**
     * A reference to the opened [CameraDevice].
     */
    protected var cameraDevice: CameraDevice? = null

    private lateinit var device: CameraHelper.Device

    private lateinit var resolution: CameraHelper.Device.Resolution

    private var isPhoto = false

    //*****************************************************************************************************************

    /**
     * Tries to open a [CameraDevice].
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        setUpCamera()

        if (!isAttachedToWindow) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                PermissionHelper.isPermissionsDenied(Manifest.permission.CAMERA)) {
            error("permission denied")
            return
        }

        if (isPhoto) {
            initImageReader()
        } else {
            initMediaRecorder()
        }

        try {
            Log.d("ebnbin", "tryAcquire")

            // Wait for camera to open - 2.5 seconds is sufficient

            // Wait for any previously running session to finish.
            if (!cameraOpenCloseLock.tryAcquire()) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            // Attempt to open the camera. StateCallback will be called on the background handler's
            // thread when this succeeds or fails.
            cameraManager.openCamera(device.id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice?) {
                    // This method is called when the camera is opened.  We start camera preview here if
                    // the TextureView displaying this has been set up.
                    cameraOpenCloseLock.release()
                    cameraDevice = camera

                    if (isPhoto) {
                        startPhotoPreview()
                    } else {
                        startVideoPreview()
                    }
                }

                override fun onDisconnected(camera: CameraDevice?) {
                    cameraOpenCloseLock.release()
                    camera?.close()
                    cameraDevice = null
                    error("open camera on disconnected")
                }

                override fun onError(camera: CameraDevice?, error: Int) {
                    Log.e("ebnbin", "Received camera device error: $error")

                    onDisconnected(camera)
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("ebnbin", "Cannot access the camera.", e)

            error("open camera access exception")
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun setUpCamera() {
        cameraOpenCloseLock = Semaphore(1)
        device = PreferenceHelper.device()
        resolution = PreferenceHelper.resolution()
        isPhoto = PreferenceHelper.isPhoto()
        surfaceTexture.setDefaultBufferSize(device.previewResolution.width, device.previewResolution.height)
        sendInvalidateBroadcast()
        invalidateTransform()
    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        if (isPhoto) {
            stopPhotoPreview()
        } else {
            stopRecord(false)
            stopVideoPreview()
        }

        try {
            cameraOpenCloseLock.acquire()
            cameraDevice?.close()
            cameraDevice = null

            if (isPhoto) {
                disposeImageReader()
            } else {
                disposeMediaRecorder()
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    //*****************************************************************************************************************

    /**
     * This is the output file.
     */
    protected lateinit var file: File private set

    protected fun setUpFile(extension: String) {
        file = FileUtil.getFile(extension)
    }

    protected fun toastFile() {
        DevHelper.event("file", mapOf("file" to file))

        if (!isAttachedToWindow) return

        if (enableToast) {
            toast("$file")
        }
        Log.d("ebnbin", "$file")
    }

    //*****************************************************************************************************************
    // Video.

    private var videoPreviewCameraCaptureSession: CameraCaptureSession? = null
    private var videoRecordCameraCaptureSession: CameraCaptureSession? = null

    private var mediaRecorder: MediaRecorder? = null

    private var isRecording = false

    private fun buildVideoPreviewCaptureRequest(cameraDevice: CameraDevice) =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(Surface(surfaceTexture))
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
            }.build()!!

    private fun buildVideoRecordCaptureRequest(cameraDevice: CameraDevice, mediaRecorder: MediaRecorder) =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                addTarget(Surface(surfaceTexture))
                addTarget(mediaRecorder.surface)
//                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
            }.build()!!

    protected fun startVideoPreview() {
        if (cameraDevice == null || !isAvailable || videoPreviewCameraCaptureSession != null) return

        val outputs = listOf(Surface(surfaceTexture))
        cameraDevice!!.createCaptureSession(outputs, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession?) {
                if (cameraDevice == null || !isAvailable || session == null) return

                val request = buildVideoPreviewCaptureRequest(cameraDevice!!)
                session.setRepeatingRequest(request, null, backgroundHandler)

                videoPreviewCameraCaptureSession = session
            }

            override fun onConfigureFailed(session: CameraCaptureSession?) {
            }
        }, backgroundHandler)
    }

    private fun stopVideoPreview() {
        videoPreviewCameraCaptureSession?.close()
        videoPreviewCameraCaptureSession = null
    }

    protected fun initMediaRecorder() {
        mediaRecorder = MediaRecorder()
    }

    protected fun disposeMediaRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    private fun setUpMediaRecorder(mediaRecorder: MediaRecorder) {
        // TODO: #14
        try {
            mediaRecorder.stop()
        } catch (e: Exception) {}
        try {
            mediaRecorder.reset()
        } catch (e: Exception) {}
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOrientationHint(device.getOrientation())
        val videoProfile = PreferenceHelper.videoProfile()
        mediaRecorder.setProfile(videoProfile.camcorderProfile)
        setUpFile(videoProfile.camcorderProfile.fileFormatExtension)
        mediaRecorder.setOutputFile(file.absolutePath)
        mediaRecorder.prepare()
    }

    private fun startRecord() {
        if (cameraDevice == null ||
                videoRecordCameraCaptureSession != null ||
                !isAvailable ||
                mediaRecorder == null ||
                isRecording) return

        videoPreviewCameraCaptureSession?.close()
        videoPreviewCameraCaptureSession = null

        try {
            setUpMediaRecorder(mediaRecorder!!)
        } catch (e: Exception) {
            Crashlytics.logException(e)
            Toast.makeText(context, R.string.camera_exception, Toast.LENGTH_SHORT).show()
            error("setUpMediaRecorder")
            return
        }

        val outputs = listOf(Surface(surfaceTexture), mediaRecorder!!.surface)
        cameraDevice!!.createCaptureSession(outputs, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession?) {
                if (cameraDevice == null || !isAvailable || session == null || mediaRecorder == null) return

                try {
                    val request = buildVideoRecordCaptureRequest(cameraDevice!!, mediaRecorder!!)
                    session.setRepeatingRequest(request, null, backgroundHandler)
                } catch (e: CameraAccessException) {
                    Crashlytics.logException(e)
                    Toast.makeText(context, R.string.camera_exception, Toast.LENGTH_SHORT).show()
                    error("start record set repeating request")
                    return
                }

                post {
                    isRecording = true
                    mediaRecorder!!.start()
                    sendVideoBroadcast(true)
                }

                videoRecordCameraCaptureSession = session
            }

            override fun onConfigureFailed(session: CameraCaptureSession?) {
            }
        }, backgroundHandler)
    }

    protected fun stopRecord(resumePreview: Boolean) {
        if (!isRecording) return

        sendVideoBroadcast(false)

        isRecording = false

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
        try {
            mediaRecorder?.reset()
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }

        toastFile()

        videoRecordCameraCaptureSession?.close()
        videoRecordCameraCaptureSession = null

        if (resumePreview) {
            startVideoPreview()
        }
    }

    protected fun toggleRecord() {
        if (isRecording) {
            stopRecord(true)
        } else {
            startRecord()
        }
    }

    //*****************************************************************************************************************
    // Photo.

    private var imageReader: ImageReader? = null

    private var photoPreviewCameraCaptureSession: CameraCaptureSession? = null

    protected fun initImageReader() {
        imageReader = ImageReader.newInstance(resolution.width, resolution.height,
                ImageFormat.JPEG, /*maxImages*/ 2).apply {
            setOnImageAvailableListener({
                backgroundHandler.post {
                    val image = it.acquireNextImage()

                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    var output: FileOutputStream? = null
                    try {
                        output = FileOutputStream(file).apply {
                            write(bytes)
                        }
                    } catch (e: IOException) {
                        Log.e("ebnbin", e.toString())
                    } finally {
                        image.close()
                        output?.let {
                            try {
                                it.close()
                            } catch (e: IOException) {
                                Log.e("ebnbin", e.toString())
                            }
                        }
                    }
                }
            }, backgroundHandler)
        }
    }

    protected fun disposeImageReader() {
        imageReader?.close()
        imageReader = null
    }

    private fun buildPhotoPreviewCaptureRequest(cameraDevice: CameraDevice) =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(Surface(surfaceTexture))
//                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }.build()!!

    private fun buildPhotoCaptureCaptureRequest(cameraDevice: CameraDevice, imageReader: ImageReader) =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReader.surface)
                set(CaptureRequest.JPEG_ORIENTATION, device.getOrientation())
//                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }.build()!!

    protected fun startPhotoPreview() {
        if (cameraDevice == null ||
                !isAvailable ||
                !isAttachedToWindow ||
                imageReader == null ||
                photoPreviewCameraCaptureSession != null) return

        val outputs = listOf(Surface(surfaceTexture), imageReader!!.surface)
        cameraDevice!!.createCaptureSession(outputs, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession?) {
                if (cameraDevice == null || session == null) return

                val request = buildPhotoPreviewCaptureRequest(cameraDevice!!)
                session.setRepeatingRequest(request, null, backgroundHandler)

                photoPreviewCameraCaptureSession = session
            }

            override fun onConfigureFailed(session: CameraCaptureSession?) {
            }
        }, backgroundHandler)
    }

    protected fun stopPhotoPreview() {
        photoPreviewCameraCaptureSession?.close()
        photoPreviewCameraCaptureSession = null
    }

    protected fun capture() {
        if (!isAvailable ||
                photoPreviewCameraCaptureSession == null ||
                cameraDevice == null ||
                imageReader == null) return

//        photoPreviewCameraCaptureSession!!.stopRepeating()
//        photoPreviewCameraCaptureSession!!.abortCaptures()
        val request = buildPhotoCaptureCaptureRequest(cameraDevice!!, imageReader!!)
        photoPreviewCameraCaptureSession!!.capture(request, object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureStarted(session: CameraCaptureSession?, request: CaptureRequest?, timestamp: Long,
                    frameNumber: Long) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)

                setUpFile(".jpg")
            }

            override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?,
                    result: TotalCaptureResult?) {
                super.onCaptureCompleted(session, request, result)

                if (photoPreviewCameraCaptureSession == null || cameraDevice == null) return

                toastFile()
//
//                val captureRequest = buildPhotoPreviewCaptureRequest(cameraDevice!!)
//                photoPreviewCameraCaptureSession!!.setRepeatingRequest(captureRequest, null, backgroundHandler)
            }
        }, backgroundHandler)
    }

    //*****************************************************************************************************************

    private fun sendInvalidateBroadcast() {
        LocalBroadcastHelper.send(ACTION_INVALIDATE)
    }

    private fun sendVideoBroadcast(isRecording: Boolean) {
        LocalBroadcastHelper.send(ACTION_VIDEO, Intent().putExtra(EXTRA_IS_RECORDING, isRecording))
    }

    companion object {
        const val ACTION_INVALIDATE = "invalidate"
        const val ACTION_VIDEO = "video"
        const val EXTRA_IS_RECORDING = "is_recording"
    }
}
