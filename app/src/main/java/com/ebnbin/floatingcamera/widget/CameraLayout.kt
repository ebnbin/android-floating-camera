package com.ebnbin.floatingcamera.widget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.ebnbin.floatingcamera.CameraService
import com.ebnbin.floatingcamera.MainActivity
import com.ebnbin.floatingcamera.fragment.preference.CameraPreferenceFragment
import com.ebnbin.floatingcamera.fragment.preference.WindowPreferenceFragment
import com.ebnbin.floatingcamera.util.DebugHelper
import com.ebnbin.floatingcamera.util.LocalBroadcastHelper
import com.ebnbin.floatingcamera.util.PreferenceHelper
import com.ebnbin.floatingcamera.util.RotationHelper
import com.ebnbin.floatingcamera.util.WindowSize
import com.ebnbin.floatingcamera.util.displayRealSize
import com.ebnbin.floatingcamera.util.displayRotation
import com.ebnbin.floatingcamera.util.sp
import com.ebnbin.floatingcamera.util.windowManager
import kotlin.math.max
import kotlin.math.min

class CameraLayout : FrameLayout,
        SharedPreferences.OnSharedPreferenceChangeListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener,
        LocalBroadcastHelper.Receiver {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private lateinit var cameraView: CameraView
    private lateinit var infoView: InfoView

    private fun init() {
        cameraView = CameraView(context)
        infoView = InfoView(context)

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        addView(cameraView, params)
        addView(infoView, params)

        invalidateWindowAlpha()
    }

    //*****************************************************************************************************************

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        LocalBroadcastHelper.register(this, CameraView.ACTION_INVALIDATE)

        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onReceive(context: Context, intent: Intent, action: String) {
        when (action) {
            CameraView.ACTION_INVALIDATE -> invalidateWindowSizeAndPosition()
        }
    }

    override fun onDetachedFromWindow() {
        sp.unregisterOnSharedPreferenceChangeListener(this)

        LocalBroadcastHelper.unregister(this)

        super.onDetachedFromWindow()
    }

    //*****************************************************************************************************************
    // 偏好.

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            WindowPreferenceFragment.KEY_WINDOW_SIZE -> invalidateWindowSizeAndPosition()
            WindowPreferenceFragment.KEY_WINDOW_X -> invalidateWindowSizeAndPosition(true)
            WindowPreferenceFragment.KEY_WINDOW_Y -> invalidateWindowSizeAndPosition(true)
            CameraPreferenceFragment.KEY_PREVIEW -> invalidateWindowSizeAndPosition()
            WindowPreferenceFragment.KEY_ENABLE_GESTURE_MOVE ->
                enableGestureMove = WindowPreferenceFragment.enableGestureMove
            WindowPreferenceFragment.KEY_ENABLE_GESTURE_SCALE ->
                enableGestureScale = WindowPreferenceFragment.enableGestureScale
            WindowPreferenceFragment.KEY_ENABLE_GESTURE_TAP ->
                enableGestureTap = WindowPreferenceFragment.enableGestureTap
            WindowPreferenceFragment.KEY_WINDOW_ALPHA -> invalidateWindowAlpha()
            RotationHelper.KEY_ROTATION -> invalidateWindowSizeAndPosition()
            WindowPreferenceFragment.KEY_IS_TOUCHABLE -> {
                val params = layoutParams as WindowManager.LayoutParams
                if (WindowPreferenceFragment.isTouchable) {
                    params.flags = params.flags xor WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                } else {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                }
                windowManager.updateViewLayout(this, params)
            }
        }
    }

    private fun invalidateWindowSizeAndPosition(invalidateWindowPositionOnly: Boolean = false) {
        val params = layoutParams as WindowManager.LayoutParams
        val rotation = displayRotation()
        val windowSize: WindowSize
        if (invalidateWindowPositionOnly) {
            windowSize = WindowSize(params.width, params.height, rotation)
        } else {
            windowSize = PreferenceHelper.windowSize()
            params.width = windowSize.width(rotation)
            params.height = windowSize.height(rotation)
        }
        val windowPosition = PreferenceHelper.windowPosition()
        params.x = windowPosition.x(windowSize, rotation)
        params.y = windowPosition.y(windowSize, rotation)
        windowManager.updateViewLayout(this, params)
    }

    private fun invalidateWindowAlpha() {
        alpha = WindowPreferenceFragment.windowAlpha / 100f
    }

    //*****************************************************************************************************************
    // 手势.

    @Suppress("LeakingThis")
    private val gestureDetector = GestureDetectorCompat(context, this)

    @Suppress("LeakingThis")
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private var enableGestureMove = WindowPreferenceFragment.enableGestureMove
    private var enableGestureScale = WindowPreferenceFragment.enableGestureScale
    private var enableGestureTap = WindowPreferenceFragment.enableGestureTap

    private var downX = 0
    private var downY = 0

    private var downRawX = 0f
    private var downRawY = 0f

    private var downRotation = 0

    private var scaleBeginWidth = 0
    private var scaleBeginHeight = 0

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

        if (enableGestureMove) {
            val layoutParams = layoutParams as WindowManager.LayoutParams
            downX = layoutParams.x
            downY = layoutParams.y

            downRawX = e.rawX
            downRawY = e.rawY

            downRotation = displayRotation()
        }

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

        if (enableGestureMove) {
            val layoutParams = layoutParams as WindowManager.LayoutParams
            layoutParams.x = (downX + e2.rawX - downRawX).toInt()
            layoutParams.y = (downY + e2.rawY - downRawY).toInt()
            windowManager.updateViewLayout(this, layoutParams)
        }

        return false
    }

    /**
     * 第一个手指长按.
     */
    override fun onLongPress(e: MotionEvent?) {
        e ?: return

        DebugHelper.log("onLongPress")

        finish()
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

        if (enableGestureTap) {
            if (cameraView.isAttachedToWindow) cameraView.onTap()
        }

        return false
    }

    /**
     * 缩放开始. 双指按下且开始移动时调用.
     */
    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return true

        DebugHelper.log("onScaleBegin")

        if (enableGestureScale) {
            scaleBeginWidth = layoutParams.width
            scaleBeginHeight = layoutParams.height
        }

        return true
    }

    /**
     * 缩放完成.
     */
    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        DebugHelper.log("onScaleEnd")

        if (enableGestureScale) {
            putWindowSize(detector.scaleFactor)
        }
    }

    /**
     * 更新窗口大小.
     */
    private fun putWindowSize(scaleFactor: Float) {
        var windowSize = (WindowPreferenceFragment.windowSize * scaleFactor).toInt()
        windowSize = min(100, windowSize)
        windowSize = max(0, windowSize)
        val sharedPreferencesWindowSize = WindowPreferenceFragment.windowSize
        if (sharedPreferencesWindowSize != windowSize) {
            WindowPreferenceFragment.putWindowSize(windowSize)
        } else {
            invalidateWindowSizeAndPosition()
        }
    }

    /**
     * 缩放.
     */
    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        DebugHelper.log("onScale")

        if (enableGestureScale) {
            val scaleFactor = detector.scaleFactor
            layoutParams.width = (scaleBeginWidth * scaleFactor).toInt()
            layoutParams.height = (scaleBeginHeight * scaleFactor).toInt()
            windowManager.updateViewLayout(this, layoutParams)
        }

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

            if (enableGestureMove) {
                val offsetX = event.rawX - downRawX
                val offsetY = event.rawY - downRawY
                val x = downX + offsetX
                val y = downY + offsetY
                val windowSize = WindowSize(layoutParams.width, layoutParams.height, downRotation)
                putWindowPosition(x, y, downRotation, windowSize)
            }
        }

        if (scaleGestureDetector.onTouchEvent(event)) return true

        return super.onTouchEvent(event)
    }

    /**
     * 更新窗口位置.
     */
    private fun putWindowPosition(x: Float, y: Float, rotation: Int, windowSize: WindowSize) {
        fun calc(position: Float, offset: Int, range: Int, percentOffset: Int) =
                ((position + offset) / range * 100).toInt() + percentOffset

        val displayRealWidth = displayRealSize.width(rotation)
        val windowWidth = windowSize.width(rotation)

        val xMin = 0
        val xMax = displayRealWidth - windowWidth

        val xRange = displayRealWidth - windowWidth
        val xOffset = 0
        val xPercentOffset = 0

        val leftRange = windowWidth
        val leftOffset = windowWidth
        val leftPercentOffset = -100

        val rightRange = windowWidth
        val rightOffset = -(displayRealWidth - windowWidth)
        val rightPercentOffset = 100

        var windowX = when {
            x in xMin..xMax -> calc(x, xOffset, xRange, xPercentOffset)
            x < xMin -> calc(x, leftOffset, leftRange, leftPercentOffset)
            else -> calc(x, rightOffset, rightRange, rightPercentOffset)
        }
        windowX = min(200, windowX)
        windowX = max(-100, windowX)

        val displayRealHeight = displayRealSize.height(rotation)
        val windowHeight = windowSize.height(rotation)

        val yMin = 0
        val yMax = displayRealHeight - windowHeight

        val yRange = displayRealHeight - windowHeight
        val yOffset = 0
        val yPercentOffset = 0

        val topRange = windowHeight
        val topOffset = windowHeight
        val topPercentOffset = -100

        val bottomRange = windowHeight
        val bottomOffset = -(displayRealHeight - windowHeight)
        val bottomPercentOffset = 100

        var windowY = when {
            y in yMin..yMax -> calc(y, yOffset, yRange, yPercentOffset)
            y < yMin -> calc(y, topOffset, topRange, topPercentOffset)
            else -> calc(y, bottomOffset, bottomRange, bottomPercentOffset)
        }
        windowY = min(200, windowY)
        windowY = max(-100, windowY)

        val sharedPreferencesWindowX = WindowPreferenceFragment.windowX
        val sharedPreferencesWindowY = WindowPreferenceFragment.windowY

        if (sharedPreferencesWindowX != windowX || sharedPreferencesWindowY != windowY) {
            WindowPreferenceFragment.putWindowPosition(windowX, windowY)
        } else {
            invalidateWindowSizeAndPosition(true)
        }
    }

    //*****************************************************************************************************************
    // Finish.

    fun finish() {
        if (!isAttachedToWindow) return

        if (cameraView.isAttachedToWindow) cameraView.finish()

        CameraService.stop()
    }
}
