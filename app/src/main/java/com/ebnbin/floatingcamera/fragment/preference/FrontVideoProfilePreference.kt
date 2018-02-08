package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.extension.getValueSize
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

/**
 * 前置摄像头视频配置偏好.
 */
class FrontVideoProfilePreference(frontVideoProfilePreferenceGroup: FrontVideoProfilePreferenceGroup) :
        ListPreference(frontVideoProfilePreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.front_video_profile_title)
        dialogTitle = title
        setEntriesAndValues(cameraHelper.frontDevice.videoProfileSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]
            frontVideoProfilePreferenceGroup.frontVideoProfileCustomPreferenceGroup.isGroupVisible =
                    newValue == entryValues[getValueSize() - 1].toString()

            true
        }

        frontVideoProfilePreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "front_video_profile"
        private const val DEF_VALUE = "0"
    }
}
