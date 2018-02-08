package com.ebnbin.floatingcamera.fragment.preference

import android.content.SharedPreferences
import android.support.v7.preference.SeekBarPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get

/**
 * 悬浮窗水平方向位置.
 */
class WindowXPreference(rootPreferenceGroup: RootPreferenceGroup) : SeekBarPreference(rootPreferenceGroup.context),
        SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.window_x_title)
        setSummary(R.string.window_x_summary)
        min = -100
        max = 200

        rootPreferenceGroup.addPreferenceToGroup(this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY) return
    }

    companion object {
        private const val KEY = "window_x"
        private const val DEF_VALUE = 50

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
