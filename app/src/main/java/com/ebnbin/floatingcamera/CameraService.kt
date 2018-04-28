package com.ebnbin.floatingcamera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.ebnbin.floatingcamera.util.LocalBroadcastHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.displayRotation
import com.ebnbin.floatingcamera.util.windowManager
import com.ebnbin.floatingcamera.widget.CameraLayout

/**
 * 相机服务.
 */
class CameraService : Service(), LocalBroadcastHelper.Receiver {
    private lateinit var cameraLayout: CameraLayout

    override fun onReceive(context: Context, intent: Intent, action: String) {
        when (action) {
            ACTION_POST_STOP -> cameraLayout.finish()
        }
    }

    override fun onCreate() {
        super.onCreate()

        LocalBroadcastHelper.register(this, ACTION_POST_STOP)

        isRunning = true
        LocalBroadcastHelper.send(ACTION_CAMERA_SERVICE_IS_RUNNING, Intent().putExtra(KEY_IS_RUNNING, isRunning))

        RotationHelper.registerAndEnable(this)

        cameraLayout = CameraLayout(this)

        val params = WindowManager.LayoutParams()
        val rotation = displayRotation()
        val windowSize = PreferenceHelper.windowSize()
        params.width = windowSize.width(rotation)
        params.height = windowSize.height(rotation)
        @Suppress("DEPRECATION")
        params.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        params.format = PixelFormat.TRANSLUCENT
        params.gravity = Gravity.START or Gravity.TOP
        val windowPosition = PreferenceHelper.windowPosition()
        val x = windowPosition.x(windowSize, rotation)
        val y = windowPosition.y(windowSize, rotation)
        params.x = x
        params.y = y

        windowManager.addView(cameraLayout, params)
    }

    override fun onDestroy() {
        windowManager.removeView(cameraLayout)

        RotationHelper.unregister(this)

        isRunning = false
        LocalBroadcastHelper.send(ACTION_CAMERA_SERVICE_IS_RUNNING, Intent().putExtra(KEY_IS_RUNNING, isRunning))

        LocalBroadcastHelper.unregister(this)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val ACTION_CAMERA_SERVICE_IS_RUNNING = "camera_service_is_running"
        private const val ACTION_POST_STOP = "post_stop"
        const val KEY_IS_RUNNING = "is_running"

        var isRunning = false
            private set

        fun start(context: Context = app) {
            context.startService(Intent(context, CameraService::class.java))
        }

        fun stop(context: Context = app) {
            context.stopService(Intent(context, CameraService::class.java))
        }

        fun postStop() {
            LocalBroadcastHelper.send(ACTION_POST_STOP)
        }
    }
}
