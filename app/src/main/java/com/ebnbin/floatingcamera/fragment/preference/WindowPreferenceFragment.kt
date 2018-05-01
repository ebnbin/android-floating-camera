package com.ebnbin.floatingcamera.fragment.preference

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.SeekBarPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.PreferenceFragment
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put
import com.ebnbin.floatingcamera.util.sp

/**
 * 窗口偏好界面.
 */
class WindowPreferenceFragment : PreferenceFragment<WindowPreferenceFragment.WindowRootPreferenceGroup>() {
    override fun createRootPreferenceGroup(context: Context) = WindowRootPreferenceGroup(context)

    /**
     *     WindowSizePreference
     *     windowXPreference
     *     windowYPreference
     *     PreviewPreference
     */
    class WindowRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
        private val displayPreferenceCategory by lazy {
            PreferenceCategory(context).apply {
                setTitle(R.string.display_title)
            }
        }

        /**
         * 悬浮窗大小.
         */
        private val windowSizePreference by lazy {
            SeekBarPreference(context).apply {
                key = KEY_WINDOW_SIZE
                setDefaultValue(DEF_VALUE_WINDOW_SIZE)
                setTitle(R.string.window_size_title)
                setSummary(R.string.window_size_summary)
                min = 1
                max = 100
            }
        }

        /**
         * 悬浮窗水平方向位置.
         */
        private val windowXPreference by lazy {
            SeekBarPreference(context).apply {
                key = KEY_WINDOW_X
                setDefaultValue(DEF_VALUE_WINDOW_X)
                setTitle(R.string.window_x_title)
                setSummary(R.string.window_x_summary)
                min = -99
                max = 199
            }
        }

        /**
         * 悬浮窗垂直方向位置.
         */
        private val windowYPreference by lazy {
            SeekBarPreference(context).apply {
                key = KEY_WINDOW_Y
                setDefaultValue(DEF_VALUE_WINDOW_Y)
                setTitle(R.string.window_y_title)
                setSummary(R.string.window_y_summary)
                min = -99
                max = 199
            }
        }

        /**
         * 悬浮窗透明度.
         */
        private val windowAlphaPreference by lazy {
            SeekBarPreference(context).apply {
                key = KEY_WINDOW_ALPHA
                setDefaultValue(DEF_WINDOW_ALPHA)
                setTitle(R.string.window_alpha_title)
                setSummary(R.string.window_alpha_summary)
                min = 1
                max = 100
            }
        }

        private val enableInfoPreference by lazy {
            SwitchPreference(context).apply {
                key = KEY_ENABLE_INFO
                setDefaultValue(DEF_ENABLE_INFO)
                setTitle(R.string.enable_info_title)
                setSummaryOff(R.string.enable_info_summary_off)
                setSummaryOn(R.string.enable_info_summary_on)
            }
        }

        private val enableToastPreference by lazy {
            SwitchPreference(context).apply {
                key = KEY_ENABLE_TOAST
                setDefaultValue(DEF_ENABLE_TOAST)
                setTitle(R.string.enable_toast_title)
                setSummaryOff(R.string.enable_toast_summary_off)
                setSummaryOn(R.string.enable_toast_summary_on)
            }
        }

        private val controlPreferenceCategory by lazy {
            PreferenceCategory(context).apply {
                setTitle(R.string.control_title)
            }
        }

        private val isTouchablePreference by lazy {
            SwitchPreference(context).apply {
                key = KEY_IS_TOUCHABLE
                setDefaultValue(DEF_IS_TOUCHABLE)
                setTitle(R.string.is_touchable_title)
                setSummaryOff(R.string.is_touchable_summary_off)
                setSummaryOn(R.string.is_touchable_summary_on)
            }
        }

        private val gestureGroup by lazy {
            PreferenceGroup(context).apply {
                initPreferences(
                        enableGestureTap,
                        gestureDoubleTap,
                        gestureLongPress,
                        enableGestureMove,
                        enableGestureScale)
                initIsGroupVisible { isTouchablePreference.isChecked }
            }
        }

        /**
         * 手势移动.
         */
        private val enableGestureMove by lazy {
            SwitchPreference(context).apply {
                key = KEY_ENABLE_GESTURE_MOVE
                setDefaultValue(DEF_ENABLE_GESTURE_MOVE)
                setTitle(R.string.enable_gesture_move_title)
                setSummaryOff(R.string.enable_gesture_move_summary_off)
                setSummaryOn(R.string.enable_gesture_move_summary_on)
            }
        }

        /**
         * 手势缩放.
         */
        private val enableGestureScale by lazy {
            SwitchPreference(context).apply {
                key = KEY_ENABLE_GESTURE_SCALE
                setDefaultValue(DEF_ENABLE_GESTURE_SCALE)
                setTitle(R.string.enable_gesture_scale_title)
                setSummaryOff(R.string.enable_gesture_scale_summary_off)
                setSummaryOn(R.string.enable_gesture_scale_summary_on)
            }
        }

        private val enableGestureTap by lazy {
            SwitchPreference(context).apply {
                key = KEY_ENABLE_GESTURE_TAP
                setDefaultValue(DEF_ENABLE_GESTURE_TAP)
                setTitle(R.string.gesture_tap_title)
                setSummaryOff(R.string.gesture_tap_summary_off)
                setSummaryOn(R.string.gesture_tap_summary_on)
            }
        }

        private val gestureDoubleTap by lazy {
            Preference(context).apply {
                setTitle(R.string.gesture_double_tap_title)
                setSummary(R.string.gesture_double_tap_summary)
            }
        }

        private val gestureLongPress by lazy {
            Preference(context).apply {
                setTitle(R.string.gesture_long_press_title)
                setSummary(R.string.gesture_long_press_summary)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?) = arrayOf(
                displayPreferenceCategory,
                windowSizePreference,
                windowXPreference,
                windowYPreference,
                windowAlphaPreference,
                enableInfoPreference,
                enableToastPreference,
                controlPreferenceCategory,
                isTouchablePreference,
                gestureGroup)

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                KEY_WINDOW_SIZE -> {
                    val newValue = windowSize

                    if (windowSizePreference.value != newValue) windowSizePreference.value = newValue
                }
                KEY_WINDOW_X -> {
                    val newValue = windowX

                    if (windowXPreference.value != newValue) windowXPreference.value = newValue
                }
                KEY_WINDOW_Y -> {
                    val newValue = windowY

                    if (windowYPreference.value != newValue) windowYPreference.value = newValue
                }
                KEY_IS_TOUCHABLE -> {
                    val newValue = isTouchable

                    gestureGroup.isGroupVisible = newValue
                }
            }
        }
    }

    companion object {
        const val KEY_WINDOW_SIZE = "window_size"
        const val KEY_WINDOW_X = "window_x"
        const val KEY_WINDOW_Y = "window_y"
        const val KEY_WINDOW_ALPHA = "window_alpha"
        const val KEY_ENABLE_GESTURE_MOVE = "enable_gesture_move"
        const val KEY_ENABLE_GESTURE_SCALE = "enable_gesture_scale"
        const val KEY_ENABLE_GESTURE_TAP = "enable_gesture_tap"
        const val KEY_ENABLE_INFO = "enable_info"
        const val KEY_IS_TOUCHABLE = "is_touchable"
        const val KEY_ENABLE_TOAST = "enable_toast"

        private const val DEF_VALUE_WINDOW_SIZE = 50
        private const val DEF_VALUE_WINDOW_X = 50
        private const val DEF_VALUE_WINDOW_Y = 50
        private const val DEF_ENABLE_GESTURE_MOVE = true
        private const val DEF_ENABLE_GESTURE_SCALE = false
        private const val DEF_ENABLE_GESTURE_TAP = true
        private const val DEF_WINDOW_ALPHA = 100
        private const val DEF_ENABLE_INFO = true
        private const val DEF_IS_TOUCHABLE = true
        private const val DEF_ENABLE_TOAST = true

        val windowSize get() = sp.get(KEY_WINDOW_SIZE, DEF_VALUE_WINDOW_SIZE)
        val windowX get() = sp.get(KEY_WINDOW_X, DEF_VALUE_WINDOW_X)
        val windowY get() = sp.get(KEY_WINDOW_Y, DEF_VALUE_WINDOW_Y)
        val enableGestureMove get() = sp.get(KEY_ENABLE_GESTURE_MOVE, DEF_ENABLE_GESTURE_MOVE)
        val enableGestureScale get() = sp.get(KEY_ENABLE_GESTURE_SCALE, DEF_ENABLE_GESTURE_SCALE)
        val enableGestureTap get() = sp.get(KEY_ENABLE_GESTURE_TAP, DEF_ENABLE_GESTURE_TAP)
        val windowAlpha get() = sp.get(KEY_WINDOW_ALPHA, DEF_WINDOW_ALPHA)
        val enableInfo get() = sp.get(KEY_ENABLE_INFO, DEF_ENABLE_INFO)
        val isTouchable get() = sp.get(KEY_IS_TOUCHABLE, DEF_IS_TOUCHABLE)
        val enableToast get() = sp.get(KEY_ENABLE_TOAST, DEF_ENABLE_TOAST)

        fun putWindowSize(windowSize: Int) {
            sp.put(KEY_WINDOW_SIZE, windowSize)
        }

        fun putWindowPosition(windowX: Int, windowY: Int) {
            sp.put(KEY_WINDOW_X, windowX)
            sp.put(KEY_WINDOW_Y, windowY)
        }
    }
}
