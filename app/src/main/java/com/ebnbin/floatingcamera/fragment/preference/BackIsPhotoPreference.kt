package com.ebnbin.floatingcamera.fragment.preference

import android.support.v14.preference.SwitchPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get

/**
 * 后置摄像头视频/照片偏好.
 */
class BackIsPhotoPreference(backPreferenceGroup: BackPreferenceGroup) : SwitchPreference(backPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.is_photo_title)
        setSummaryOff(R.string.back_is_photo_summary_off)
        setSummaryOn(R.string.back_is_photo_summary_on)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as Boolean

            backPreferenceGroup.backVideoPreferenceGroup.isGroupVisible = !newValue
            backPreferenceGroup.backPhotoPreferenceGroup.isGroupVisible = newValue

            true
        }

        backPreferenceGroup.addPreferenceToGroup(this)
    }

    companion object {
        private const val KEY = "back_is_photo"
        private const val DEF_VALUE = false

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
