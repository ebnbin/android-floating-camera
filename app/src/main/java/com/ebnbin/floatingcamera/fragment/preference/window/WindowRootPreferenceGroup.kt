package com.ebnbin.floatingcamera.fragment.preference.window

import android.content.Context
import android.content.SharedPreferences
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.event.WindowSizeEvent
import com.ebnbin.floatingcamera.event.WindowXEvent
import com.ebnbin.floatingcamera.event.WindowYEvent
import com.ebnbin.floatingcamera.preference.FooterPreference
import com.ebnbin.floatingcamera.preference.ListPreference
import com.ebnbin.floatingcamera.preference.PreferenceGroup
import com.ebnbin.floatingcamera.preference.RootPreferenceGroup
import com.ebnbin.floatingcamera.preference.SeekBarPreference
import com.ebnbin.floatingcamera.util.Preview
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.eventBus
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.getString

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
                        previewPreference))
    }

    /**
     * 悬浮窗大小.
     */
    private val windowSizePreference by lazy {
        SeekBarPreference(context,
                key = KEY_WINDOW_SIZE,
                defaultValue = DEF_VALUE_WINDOW_SIZE,
                title = getString(R.string.window_size_title),
                summary = getString(R.string.window_size_summary),
                min = 0,
                max = 100)
    }

    /**
     * 悬浮窗水平方向位置.
     */
    private val windowXPreference by lazy {
        SeekBarPreference(context,
                key = KEY_WINDOW_X,
                defaultValue = DEF_VALUE_WINDOW_X,
                title = getString(R.string.window_x_title),
                summary = getString(R.string.window_x_summary),
                min = -100,
                max = 200)
    }

    /**
     * 悬浮窗垂直方向位置.
     */
    private val windowYPreference by lazy {
        SeekBarPreference(context,
                key = KEY_WINDOW_Y,
                defaultValue = DEF_VALUE_WINDOW_Y,
                title = getString(R.string.window_y_title),
                summary = getString(R.string.window_y_summary),
                min = -100,
                max = 200)
    }

    /**
     * 预览.
     */
    private val previewPreference by lazy {
        ListPreference(context,
                key = KEY_PREVIEW,
                defaultValue = DEF_VALUE_PREVIEW,
                title = getString(R.string.preview_title),
                entries = Preview.entries,
                summaries = Preview.entries,
                dialogTitle = getString(R.string.preview_title))
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
                eventBus.post(WindowSizeEvent())
            }
            KEY_WINDOW_X -> {
                eventBus.post(WindowXEvent())
            }
            KEY_WINDOW_Y -> {
                eventBus.post(WindowYEvent())
            }
            KEY_PREVIEW -> {
                eventBus.post(WindowSizeEvent())
            }
        }
    }

    companion object {
        private const val KEY_WINDOW_SIZE = "window_size"
        private const val KEY_WINDOW_X = "window_x"
        private const val KEY_WINDOW_Y = "window_y"
        private const val KEY_PREVIEW = "preview"

        private const val DEF_VALUE_WINDOW_SIZE = 50
        private const val DEF_VALUE_WINDOW_X = 50
        private const val DEF_VALUE_WINDOW_Y = 50
        private val DEF_VALUE_PREVIEW = Preview.CAPTURE.indexString

        val preview get() = defaultSharedPreferences.get(KEY_PREVIEW, DEF_VALUE_PREVIEW)
        val windowSize get() = defaultSharedPreferences.get(KEY_WINDOW_SIZE, DEF_VALUE_WINDOW_SIZE)
        val windowX get() = defaultSharedPreferences.get(KEY_WINDOW_X, DEF_VALUE_WINDOW_X)
        val windowY get() = defaultSharedPreferences.get(KEY_WINDOW_Y, DEF_VALUE_WINDOW_Y)
    }
}
