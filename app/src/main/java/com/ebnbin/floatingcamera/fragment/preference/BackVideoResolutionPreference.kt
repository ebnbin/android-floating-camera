package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.cameraHelper
import com.ebnbin.floatingcamera.setEntriesAndEntryValues

/**
 * 后置摄像头视频分辨率偏好.
 */
class BackVideoResolutionPreference(backVideoPreferenceGroup: BackVideoPreferenceGroup) :
        ListPreference(backVideoPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.back_video_resolution_title)
        dialogTitle = title
        setEntriesAndEntryValues(cameraHelper.backDevice.videoResolutionSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        backVideoPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "back_video_resolution"
        private const val DEF_VALUE = "0"
    }
}
