package com.ebnbin.floatingcamera.fragment.preference

import android.support.v14.preference.SwitchPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get

/**
 * 前置摄像头视频/照片偏好.
 */
class FrontIsPhotoPreference(frontPreferenceGroup: FrontPreferenceGroup) :
        SwitchPreference(frontPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.is_photo_title)
        setSummaryOff(R.string.front_is_photo_summary_off)
        setSummaryOn(R.string.front_is_photo_summary_on)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as Boolean

            frontPreferenceGroup.frontVideoPreferenceGroup.isGroupVisible = !newValue
            frontPreferenceGroup.frontPhotoPreferenceGroup.isGroupVisible = newValue

            true
        }

        frontPreferenceGroup.addPreferenceToGroup(this)
    }

    companion object {
        private const val KEY = "front_is_photo"
        private const val DEF_VALUE = false

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
