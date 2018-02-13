package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import android.view.WindowManager
import com.ebnbin.floatingcamera.MainActivity
import com.ebnbin.floatingcamera.event.StopServiceEvent
import com.ebnbin.floatingcamera.event.WindowScaleEvent
import com.ebnbin.floatingcamera.event.WindowScrollEvent
import com.ebnbin.floatingcamera.event.WindowSizeEvent
import com.ebnbin.floatingcamera.event.WindowXEvent
import com.ebnbin.floatingcamera.event.WindowYEvent
import com.ebnbin.floatingcamera.fragment.preference.window.WindowRootPreferenceGroup
import com.ebnbin.floatingcamera.util.DebugHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.eventBus
import com.ebnbin.floatingcamera.util.getDisplaySize
import com.ebnbin.floatingcamera.util.windowManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.min

/**
 * 相机控件.
 */
abstract class CameraView : TextureView, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        eventBus.register(this)
    }

    //*****************************************************************************************************************
    // 手势.

    @Suppress("LeakingThis")
    private val gestureDetector = GestureDetectorCompat(context, this)

    @Suppress("LeakingThis")
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var downX = 0
    private var downY = 0

    private var scaleBeginWidth = 0
    private var scaleBeginHeight = 0

    private var maxWidth = 0
    private var maxHeight = 0

    override fun onShowPress(e: MotionEvent?) {
        e ?: return

        DebugHelper.log("onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onSingleTapUp")

        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDown")

        val layoutParams = layoutParams as WindowManager.LayoutParams
        downX = layoutParams.x
        downY = layoutParams.y

        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1 == null || e2 == null) return false

        DebugHelper.log("onFling")

        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null || e2 == null) return false

        DebugHelper.log("onScroll")

        val layoutParams = layoutParams as WindowManager.LayoutParams
        layoutParams.x = (downX + e2.rawX - e1.rawX).toInt()
        layoutParams.y = (downY + e2.rawY - e1.rawY).toInt()
        windowManager.updateViewLayout(this, layoutParams)

        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        e ?: return

        DebugHelper.log("onLongPress")

        eventBus.post(StopServiceEvent())
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDoubleTap")

        MainActivity.launch()

        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDoubleTapEvent")

        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onSingleTapConfirmed")

        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return true

        DebugHelper.log("onScaleBegin")

        scaleBeginWidth = layoutParams.width
        scaleBeginHeight = layoutParams.height

        // TODO: Size 的计算需要约束比例.
        val size = getDisplaySize()
        maxWidth = size.width
        maxHeight = size.height

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        DebugHelper.log("onScaleEnd")

        WindowRootPreferenceGroup.putWindowSize(
                min(100, (WindowRootPreferenceGroup.windowSize * detector.scaleFactor).toInt()))
        eventBus.post(WindowScaleEvent())
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        DebugHelper.log("onScale")

        val scaleFactor = detector.scaleFactor
        layoutParams.width = min(maxWidth, (scaleBeginWidth * scaleFactor).toInt())
        layoutParams.height = min(maxHeight, (scaleBeginHeight * scaleFactor).toInt())
        windowManager.updateViewLayout(this, layoutParams)

        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        if (gestureDetector.onTouchEvent(event)) return true

        if (event.action == MotionEvent.ACTION_UP) {
            DebugHelper.log("ACTION_UP")

            // TODO: SharedPreferences 保存 window x, y.

            eventBus.post(WindowScrollEvent())
        }

        if (scaleGestureDetector.onTouchEvent(event)) return true

        return super.onTouchEvent(event)
    }

    //*****************************************************************************************************************

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: WindowSizeEvent) {
        val params = layoutParams as WindowManager.LayoutParams
        val windowSize = PreferenceHelper.windowSize()
        params.width = windowSize.width()
        params.height = windowSize.height()
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
