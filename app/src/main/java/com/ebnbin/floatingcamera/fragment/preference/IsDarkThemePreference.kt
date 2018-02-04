package com.ebnbin.floatingcamera.fragment.preference

import android.content.SharedPreferences
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.defaultSharedPreferences
import com.ebnbin.floatingcamera.event.IsDarkThemeEvent
import com.ebnbin.floatingcamera.get
import org.greenrobot.eventbus.EventBus

/**
 * 主题偏好.
 */
class IsDarkThemePreference(preferenceGroup: PreferenceGroup) : SwitchPreference(preferenceGroup.context),
        SharedPreferences.OnSharedPreferenceChangeListener {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.is_dark_theme_title)
        setSummaryOff(R.string.is_dark_theme_summary_off)
        setSummaryOn(R.string.is_dark_theme_summary_on)

        preferenceGroup.addPreference(this)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != KEY) return

        EventBus.getDefault().post(IsDarkThemeEvent())
    }

    companion object {
        private const val KEY = "is_dark_theme"
        private const val DEF_VALUE = false

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
