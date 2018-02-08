package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.defaultSharedPreferences
import com.ebnbin.floatingcamera.util.extension.get
import com.ebnbin.floatingcamera.util.extension.getValueSize
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

/**
 * 后置摄像头视频配置偏好.
 */
class BackVideoProfilePreference(backVideoProfilePreferenceGroup: BackVideoProfilePreferenceGroup) :
        ListPreference(backVideoProfilePreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.back_video_profile_title)
        dialogTitle = title
        setEntriesAndValues(cameraHelper.backDevice.videoProfileSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]
            backVideoProfilePreferenceGroup.backVideoProfileCustomPreferenceGroup.isGroupVisible =
                    newValue == entryValues[getValueSize() - 1].toString()

            true
        }

        backVideoProfilePreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "back_video_profile"
        private const val DEF_VALUE = "0"

        val value get() = defaultSharedPreferences.get(KEY, DEF_VALUE)
    }
}
