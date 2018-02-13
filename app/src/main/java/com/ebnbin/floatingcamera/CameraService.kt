package com.ebnbin.floatingcamera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import com.ebnbin.floatingcamera.event.StopServiceEvent
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.app
import com.ebnbin.floatingcamera.util.eventBus
import com.ebnbin.floatingcamera.util.windowManager
import com.ebnbin.floatingcamera.widget.Camera2BasicTextureView
import com.ebnbin.floatingcamera.widget.CameraView
import com.ebnbin.floatingcamera.widget.JCamera2VideoTextureView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 相机服务.
 */
class CameraService : Service() {
    private lateinit var cameraView: CameraView

    override fun onCreate() {
        super.onCreate()

        eventBus.register(this)

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
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        windowManager.addView(cameraView, params)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: StopServiceEvent) {
        stopSelf()
    }

    override fun onDestroy() {
        windowManager.removeView(cameraView)

        RotationHelper.unregister(this)

        eventBus.unregister(this)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start(context: Context = app) {
            context.startService(Intent(context, CameraService::class.java))
        }
    }
}
