package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import android.view.WindowManager
import com.ebnbin.floatingcamera.CameraService
import com.ebnbin.floatingcamera.MainActivity
import com.ebnbin.floatingcamera.fragment.preference.window.WindowRootPreferenceGroup
import com.ebnbin.floatingcamera.util.DebugHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.windowManager
import kotlin.math.max
import kotlin.math.min

/**
 * 相机控件.
 */
abstract class CameraView : TextureView,
        SharedPreferences.OnSharedPreferenceChangeListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    //*****************************************************************************************************************

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onDetachedFromWindow()
    }

    //*****************************************************************************************************************
    // 偏好.

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            WindowRootPreferenceGroup.KEY_WINDOW_SIZE -> {
                invalidateWindowSizeAndPosition()
            }
            WindowRootPreferenceGroup.KEY_WINDOW_X -> {
                invalidateWindowPosition()
            }
            WindowRootPreferenceGroup.KEY_WINDOW_Y -> {
                invalidateWindowPosition()
            }
            WindowRootPreferenceGroup.KEY_PREVIEW -> {
                invalidateWindowSizeAndPosition()
            }
        }
    }

    private fun invalidateWindowSizeAndPosition() {
        val params = layoutParams as WindowManager.LayoutParams
        val windowSize = PreferenceHelper.windowSize()
        params.width = windowSize.width()
        params.height = windowSize.height()
        // TODO: 更新窗口位置.
        windowManager.updateViewLayout(this, params)
    }

    private fun invalidateWindowPosition() {
        // TODO: 更新窗口位置.
    }

    //*****************************************************************************************************************
    // 手势.

    @Suppress("LeakingThis")
    private val gestureDetector = GestureDetectorCompat(context, this)

    @Suppress("LeakingThis")
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var downX = 0
    private var downY = 0

    private var minX = 0
    private var minY = 0
    private var maxX = 0
    private var maxY = 0

    private var scaleBeginWidth = 0
    private var scaleBeginHeight = 0

    private var maxWidth = 0
    private var maxHeight = 0

    /**
     * 按下一小会儿. 比 tap 长, 比 long press 短, 用于显示提示信息.
     */
    override fun onShowPress(e: MotionEvent?) {
        e ?: return

        DebugHelper.log("onShowPress")
    }

    /**
     * 单击 up. 还不能确定是单击还是双击.
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onSingleTapUp")

        return false
    }

    /**
     * 第一个手指按下. 第二个手指按下不会调用.
     */
    override fun onDown(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDown")

        val layoutParams = layoutParams as WindowManager.LayoutParams
        downX = layoutParams.x
        downY = layoutParams.y

        // TODO: min, max.
        minX = 0
        maxX = 0
        minY = 0
        maxY = 0

        return false
    }

    /**
     * 自由滚动. 滚动时不能保证调用.
     */
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1 == null || e2 == null) return false

        DebugHelper.log("onFling")

        return false
    }

    /**
     * 滚动.
     */
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null || e2 == null) return false

        DebugHelper.log("onScroll")

        val layoutParams = layoutParams as WindowManager.LayoutParams
        // TODO: min, max.
        layoutParams.x = (downX + e2.rawX - e1.rawX).toInt()
        layoutParams.y = (downY + e2.rawY - e1.rawY).toInt()
        windowManager.updateViewLayout(this, layoutParams)

        return false
    }

    /**
     * 第一个手指长按.
     */
    override fun onLongPress(e: MotionEvent?) {
        e ?: return

        DebugHelper.log("onLongPress")

        CameraService.stop()
    }

    /**
     * 双击. 已确定双击, 也就是第二击 down 时调用.
     */
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDoubleTap")

        MainActivity.start()

        return false
    }

    /**
     * 双击事件. 已确定双击后, 第二击的所有事件, 包括 down, move 和 up.
     */
    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onDoubleTapEvent")

        return false
    }

    /**
     * 单击. 已确定是单击, 之后没有双击时调用.
     */
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        e ?: return false

        DebugHelper.log("onSingleTapConfirmed")

        return false
    }

    /**
     * 缩放开始. 双指按下且开始移动时调用.
     */
    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return true

        DebugHelper.log("onScaleBegin")

        scaleBeginWidth = layoutParams.width
        scaleBeginHeight = layoutParams.height

        val maxWindowSize = PreferenceHelper.maxWindowSize()
        maxWidth = maxWindowSize.width()
        maxHeight = maxWindowSize.height()

        return true
    }

    /**
     * 缩放完成.
     */
    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        DebugHelper.log("onScaleEnd")

        PreferenceHelper.putWindowSize(detector.scaleFactor)
    }

    /**
     * 缩放.
     */
    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        DebugHelper.log("onScale")

        val scaleFactor = detector.scaleFactor
        var width = (scaleBeginWidth * scaleFactor).toInt()
        width = min(maxWidth, width)
        width = max(0, width)
        layoutParams.width = width
        var height = (scaleBeginHeight * scaleFactor).toInt()
        height = min(maxHeight, height)
        height = max(0, height)
        layoutParams.height = height
        windowManager.updateViewLayout(this, layoutParams)

        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        if (gestureDetector.onTouchEvent(event)) return true

        /**
         * 第一个 down 事件 up 时调用, 第二个 down 事件 up 时不会调用.
         */
        if (event.action == MotionEvent.ACTION_UP) {
            DebugHelper.log("ACTION_UP")

            // TODO: SharedPreferences 保存 window x, y.
        }

        if (scaleGestureDetector.onTouchEvent(event)) return true

        return super.onTouchEvent(event)
    }
}
