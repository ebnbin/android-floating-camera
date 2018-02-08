package com.ebnbin.floatingcamera.fragment.preference

import android.support.v7.preference.ListPreference
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.cameraHelper
import com.ebnbin.floatingcamera.util.extension.setEntriesAndValues

/**
 * 后置摄像头照片分辨率偏好.
 */
class BackPhotoResolutionPreference(backPhotoPreferenceGroup: BackPhotoPreferenceGroup) :
        ListPreference(backPhotoPreferenceGroup.context) {
    init {
        key = KEY
        setDefaultValue(DEF_VALUE)
        setTitle(R.string.back_photo_resolution_title)
        dialogTitle = title
        setEntriesAndValues(cameraHelper.backDevice.photoResolutionSummaries)
        setOnPreferenceChangeListener { _, newValue ->
            newValue as String

            summary = entries[newValue.toInt()]

            true
        }

        backPhotoPreferenceGroup.addPreferenceToGroup(this)

        summary = entry
    }

    companion object {
        private const val KEY = "back_photo_resolution"
        private const val DEF_VALUE = "0"
    }
}
