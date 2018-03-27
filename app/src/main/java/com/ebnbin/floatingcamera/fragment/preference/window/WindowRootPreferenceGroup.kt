package com.ebnbin.floatingcamera.fragment.preference.window

import android.content.Context
import android.content.SharedPreferences
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.SeekBarPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.ListPreference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.util.Preview
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.put
import com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues

/**
 * 窗口根偏好组.
 *
 *     WindowPreferenceGroup
 *         WindowSizePreference
 *         windowXPreference
 *         windowYPreference
 *         PreviewPreference
 *     FooterPreference
 */
class WindowRootPreferenceGroup(context: Context) : RootPreferenceGroup(context) {
    /**
     * 窗口偏好组.
     */
    private val windowPreferenceGroup by lazy {
        PreferenceGroup(context,
                preferences = arrayOf(
                        windowSizePreference,
                        windowXPreference,
                        windowYPreference,
                        previewPreference,
                        enableGestureMove,
                        enableGestureScale))
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
            min = -100
            max = 200
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
            min = -100
            max = 200
        }
    }

    /**
     * 预览.
     */
    private val previewPreference by lazy {
        ListPreference(context).apply {
            key = KEY_PREVIEW
            setDefaultValue(DEF_VALUE_PREVIEW)
            setTitle(R.string.preview_title)
            setEntriesAndEntryValues(Preview.entries)
            summaries = Preview.entries
            setDialogTitle(R.string.preview_title)
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

    /**
     * 底部偏好.
     */
    private val footerPreference by lazy {
        FooterPreference(context)
    }

    override fun preferences() = arrayOf(
            windowPreferenceGroup,
            footerPreference)

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
        }
    }

    companion object {
        const val KEY_WINDOW_SIZE = "window_size"
        const val KEY_WINDOW_X = "window_x"
        const val KEY_WINDOW_Y = "window_y"
        const val KEY_PREVIEW = "preview"
        const val KEY_ENABLE_GESTURE_MOVE = "enable_gesture_move"
        const val KEY_ENABLE_GESTURE_SCALE = "enable_gesture_scale"

        private const val DEF_VALUE_WINDOW_SIZE = 50
        private const val DEF_VALUE_WINDOW_X = 50
        private const val DEF_VALUE_WINDOW_Y = 50
        private val DEF_VALUE_PREVIEW = Preview.CAPTURE.indexString
        private const val DEF_ENABLE_GESTURE_MOVE = false
        private const val DEF_ENABLE_GESTURE_SCALE = false

        val preview get() = defaultSharedPreferences.get(KEY_PREVIEW, DEF_VALUE_PREVIEW)
        val windowSize get() = defaultSharedPreferences.get(KEY_WINDOW_SIZE, DEF_VALUE_WINDOW_SIZE)
        val windowX get() = defaultSharedPreferences.get(KEY_WINDOW_X, DEF_VALUE_WINDOW_X)
        val windowY get() = defaultSharedPreferences.get(KEY_WINDOW_Y, DEF_VALUE_WINDOW_Y)
        val enableGestureMove get() = defaultSharedPreferences.get(KEY_ENABLE_GESTURE_MOVE, DEF_ENABLE_GESTURE_MOVE)
        val enableGestureScale get() = defaultSharedPreferences.get(KEY_ENABLE_GESTURE_SCALE, DEF_ENABLE_GESTURE_SCALE)

        fun putWindowSize(windowSize: Int) {
            defaultSharedPreferences.put(KEY_WINDOW_SIZE, windowSize)
        }

        fun putWindowPosition(windowX: Int, windowY: Int) {
            defaultSharedPreferences.put(KEY_WINDOW_X, windowX)
            defaultSharedPreferences.put(KEY_WINDOW_Y, windowY)
        }
    }
}
