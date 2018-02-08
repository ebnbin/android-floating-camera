package com.ebnbin.floatingcamera.fragment.preference

import android.content.SharedPreferences
import android.support.v7.preference.SeekBarPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get

/**
 * 悬浮窗垂直方向位置.
 */
class WindowYPreference(rootPreferenceGroup: RootPreferenceGroup) : SeekBarPreference(rootPreferenceGroup.context),
        SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.window_y_title)
        setSummary(R.string.window_y_summary)
        min = -100
        max = 200

        rootPreferenceGroup.addPreferenceToGroup(this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY) return
    }

    companion object {
        private const val KEY = "window_y"
        private const val DEF_VALUE = 50

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
