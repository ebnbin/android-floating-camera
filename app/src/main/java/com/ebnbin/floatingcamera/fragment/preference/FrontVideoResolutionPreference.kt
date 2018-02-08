package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

/**
 * 前置摄像头视频分辨率偏好.
 */
class FrontVideoResolutionPreference(frontVideoProfileCustomPreferenceGroup: FrontVideoProfileCustomPreferenceGroup) :
        ListPreference(frontVideoProfileCustomPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.front_video_resolution_title)
        dialogTitle = title
        setEntriesAndValues(cameraHelper.frontDevice.videoResolutionSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        frontVideoProfileCustomPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "front_video_resolution"
        private const val DEF_VALUE = "0"
    }
}
