package com.ebnbin.floatingcamera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.windowManager
import com.ebnbin.floatingcamera.widget.Camera2BasicTextureView
import com.ebnbin.floatingcamera.widget.CameraView
import com.ebnbin.floatingcamera.widget.JCamera2VideoTextureView

/**
 * 相机服务.
 */
class CameraService : Service() {
    private lateinit var cameraView: CameraView

    override fun onCreate() {
        super.onCreate()

        RotationHelper.registerAndEnable(this)

        cameraView = if (PreferenceHelper.isPhoto())
            Camera2BasicTextureView(this) else
            JCamera2VideoTextureView(this)

        val params = WindowManager.LayoutParams()
        val windowSize = PreferenceHelper.windowSize()
        params.width = windowSize.width()
        params.height = windowSize.height()
        @Suppress("DEPRECATION")
        params.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        params.gravity = Gravity.START or Gravity.TOP

        windowManager.addView(cameraView, params)
    }

    override fun onDestroy() {
        windowManager.removeView(cameraView)

        RotationHelper.unregister(this)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start(context: Context = app) {
            context.startService(Intent(context, CameraService::class.java))
        }

        fun stop(context: Context = app) {
            context.stopService(Intent(context, CameraService::class.java))
        }
    }
}
