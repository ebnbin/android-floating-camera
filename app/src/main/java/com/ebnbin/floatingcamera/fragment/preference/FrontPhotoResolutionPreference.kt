package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.cameraHelper
import com.ebnbin.floatingcamera.setEntriesAndEntryValues

/**
 * 前置摄像头照片分辨率偏好.
 */
class FrontPhotoResolutionPreference(frontPhotoPreferenceGroup: FrontPhotoPreferenceGroup) :
        ListPreference(frontPhotoPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.front_photo_resolution_title)
        dialogTitle = title
        setEntriesAndEntryValues(cameraHelper.frontDevice.photoResolutionSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        frontPhotoPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "front_photo_resolution"
        private const val DEF_VALUE = "0"
    }
}
