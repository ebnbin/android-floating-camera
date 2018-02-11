package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.WindowManager
import com.ebnbin.floatingcamera.event.WindowSizeEvent
import com.ebnbin.floatingcamera.event.WindowXEvent
import com.ebnbin.floatingcamera.event.WindowYEvent
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.eventBus
import com.ebnbin.floatingcamera.util.isDisplayRotationLandscape
import com.ebnbin.floatingcamera.util.windowManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 相机控件.
 */
abstract class CameraView : TextureView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: WindowSizeEvent) {
        val windowSize = PreferenceHelper.windowSize()
        val isDisplayRotationLandscape = isDisplayRotationLandscape()

        val params = layoutParams as WindowManager.LayoutParams
        params.width = if (isDisplayRotationLandscape) windowSize.landscapeWidth else windowSize.landscapeHeight
        params.height = if (isDisplayRotationLandscape) windowSize.landscapeHeight else windowSize.landscapeWidth
        // TODO: 更新窗口位置.
        windowManager.updateViewLayout(this, params)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: WindowXEvent) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: WindowYEvent) {
    }

    override fun onDetachedFromWindow() {
        eventBus.unregister(this)

        super.onDetachedFromWindow()
    }
}
