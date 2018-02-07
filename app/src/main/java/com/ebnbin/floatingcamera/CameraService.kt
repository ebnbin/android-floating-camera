package com.ebnbin.floatingcamera

import android.app.Service
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.windowManager
import com.ebnbin.floatingcamera.widget.CameraView
import com.ebnbin.floatingcamera.widget.JCamera2VideoTextureView

/**
 * 相机服务.
 */
class CameraService : Service() {
    private lateinit var cameraView: CameraView

    override fun onCreate() {
        super.onCreate()

        cameraView = JCamera2VideoTextureView(this)
        cameraView.setOnClickListener { stopSelf() }

        val params = WindowManager.LayoutParams()
        params.width = 540
        params.height = 960
        @Suppress("DEPRECATION")
        params.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        windowManager.addView(cameraView, params)
    }

    override fun onDestroy() {
        windowManager.removeView(cameraView)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start() {
            app.startService(Intent(app, CameraService::class.java))
        }
    }
}
