package com.ebnbin.floatingcamera

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.displayRotation
import com.ebnbin.floatingcamera.util.localBroadcastManager
import com.ebnbin.floatingcamera.util.windowManager
import com.ebnbin.floatingcamera.widget.Camera2BasicTextureView
import com.ebnbin.floatingcamera.widget.CameraView
import com.ebnbin.floatingcamera.widget.JCamera2VideoTextureView

/**
 * 相机服务.
 */
class CameraService : Service() {
    private lateinit var cameraView: CameraView

    private val postStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            cameraView.finish()
        }
    }

    override fun onCreate() {
        super.onCreate()

        localBroadcastManager.registerReceiver(postStopReceiver, IntentFilter(ACTION_POST_STOP))

        isRunning = true
        localBroadcastManager.sendBroadcast(Intent(ACTION_CAMERA_SERVICE_IS_RUNNING)
                .putExtra(KEY_IS_RUNNING, isRunning))

        RotationHelper.registerAndEnable(this)

        cameraView = if (PreferenceHelper.isPhoto())
            Camera2BasicTextureView(this) else
            JCamera2VideoTextureView(this)

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
        params.gravity = Gravity.START or Gravity.TOP
        val windowPosition = PreferenceHelper.windowPosition()
        val x = windowPosition.x(windowSize, rotation)
        val y = windowPosition.y(windowSize, rotation)
        params.x = x
        params.y = y

        windowManager.addView(cameraView, params)
    }

    override fun onDestroy() {
        windowManager.removeView(cameraView)

        RotationHelper.unregister(this)

        isRunning = false
        localBroadcastManager.sendBroadcast(Intent(ACTION_CAMERA_SERVICE_IS_RUNNING)
                .putExtra(KEY_IS_RUNNING, isRunning))

        localBroadcastManager.unregisterReceiver(postStopReceiver)

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
            localBroadcastManager.sendBroadcast(Intent(ACTION_POST_STOP))
        }
    }
}
