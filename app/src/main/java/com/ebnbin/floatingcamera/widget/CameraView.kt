package com.ebnbin.floatingcamera.widget

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.PermissionHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.cameraManager
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.displayRotation
import java.io.File
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * 相机控件.
 */
abstract class CameraView : TextureView,
        TextureView.SurfaceTextureListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        RotationHelper.Listener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    //*****************************************************************************************************************

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        RotationHelper.listeners.add(this)

        startBackgroundThread()

        surfaceTextureListener = this
    }

    override fun onDetachedFromWindow() {
        stopBackgroundThread()

        RotationHelper.listeners.remove(this)

        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onDetachedFromWindow()
    }

    //*****************************************************************************************************************

    abstract fun onTap()

    //*****************************************************************************************************************

    fun finish() {
        if (isNotAttachedToWindow()) return

        beforeCloseCamera()

        closeCamera()
    }

    protected open fun beforeCloseCamera() = Unit

    //*****************************************************************************************************************

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }

    //*****************************************************************************************************************

    override fun onRotationChanged(oldRotation: Int, newRotation: Int) {
        if (isAvailable) {
            configureTransform()
        }
    }

    //*****************************************************************************************************************

    protected fun isNotAttachedToWindow() = !isAttachedToWindow

    protected val device = PreferenceHelper.device()

    protected val previewResolution = PreferenceHelper.previewResolution()

    protected val resolution = PreferenceHelper.resolution()

    protected fun toast(text: CharSequence) {
        post {
            if (isNotAttachedToWindow()) return@post

            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    //*****************************************************************************************************************

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        configureTransform()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

    //*****************************************************************************************************************

    /**
     * 配置 [setTransform].
     */
    protected fun configureTransform() {
        if (isNotAttachedToWindow()) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val viewCenterX = 0.5f * viewWidth
        val viewCenterY = 0.5f * viewHeight

        // 因为之后需要 rotate, 且以 0 度为标准, 因此这里使用 portraitWidth 和 portraitHeight.
        val bufferWidth = previewResolution.portraitWidth.toFloat()
        val bufferHeight = previewResolution.portraitHeight.toFloat()
        val bufferCenterX = 0.5f * bufferWidth
        val bufferCenterY = 0.5f * bufferHeight

        val offsetX = viewCenterX - bufferCenterX
        val offsetY = viewCenterY - bufferCenterY

        val viewRectF = RectF(0f, 0f, viewWidth, viewHeight)
        val bufferRectF = RectF(offsetX, offsetY, bufferWidth + offsetX, bufferHeight + offsetY)

        val rotation = displayRotation()

        val matrix = Matrix()

        matrix.setRectToRect(viewRectF, bufferRectF, Matrix.ScaleToFit.FILL)

        val scale = Math.max(viewWidth / previewResolution.width(rotation),
                viewHeight / previewResolution.height(rotation))
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
    protected val cameraOpenCloseLock = Semaphore(1)

    /**
     * A reference to the opened [CameraDevice].
     */
    protected var cameraDevice: CameraDevice? = null

    protected open fun beforeOpenCamera() = Unit

    /**
     * Tries to open a [CameraDevice].
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (isNotAttachedToWindow()) return

        if (PermissionHelper.isPermissionsDenied(Manifest.permission.CAMERA)) {
            finish()
            return
        }

        beforeOpenCamera()

        configureTransform()

        try {
            Log.d("ebnbin", "tryAcquire")

            // Wait for camera to open - 2.5 seconds is sufficient

            // Wait for any previously running session to finish.
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
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
                    onOpened()
                }

                override fun onDisconnected(camera: CameraDevice?) {
                    cameraOpenCloseLock.release()
                    camera?.close()
                    cameraDevice = null
                    finish()
                }

                override fun onError(camera: CameraDevice?, error: Int) {
                    Log.e("ebnbin", "Received camera device error: $error")

                    onDisconnected(camera)
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("ebnbin", "Cannot access the camera.", e)

            finish()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    protected open fun onOpened() = Unit

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            cameraDevice?.close()
            cameraDevice = null
            onCloseCamera()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    protected open fun onCloseCamera() = Unit

    //*****************************************************************************************************************

    /**
     * This is the output file.
     */
    protected lateinit var file: File private set

    protected fun setUpFile(extension: String) {
        file = FileUtil.getFile(extension)
    }

    protected fun toastFile() {
        toast("$file")
        Log.d("ebnbin", "$file")
    }
}
