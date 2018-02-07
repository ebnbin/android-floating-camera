package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.extension.setEntriesAndEntryValues

/**
 * 前置摄像头视频分辨率偏好.
 */
class FrontVideoResolutionPreference(frontVideoPreferenceGroup: FrontVideoPreferenceGroup) :
        ListPreference(frontVideoPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.front_video_resolution_title)
        dialogTitle = title
        setEntriesAndEntryValues(cameraHelper.frontDevice.videoResolutionSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        frontVideoPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "front_video_resolution"
        private const val DEF_VALUE = "0"
    }
}
