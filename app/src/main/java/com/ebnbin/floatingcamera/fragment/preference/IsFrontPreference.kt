package com.ebnbin.floatingcamera.fragment.preference

import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.PreferenceGroup
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.defaultSharedPreferences
import com.ebnbin.floatingcamera.get

/**
 * 后置/前置摄像头偏好.
 */
class IsFrontPreference(preferenceGroup: PreferenceGroup) : SwitchPreference(preferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.is_front_title)
        setSummaryOff(R.string.is_front_summary_off)
        setSummaryOn(R.string.is_front_summary_on)

        preferenceGroup.addPreference(this)
    }

    companion object {
        private const val KEY = "is_front"
        private const val DEF_VALUE = false

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
